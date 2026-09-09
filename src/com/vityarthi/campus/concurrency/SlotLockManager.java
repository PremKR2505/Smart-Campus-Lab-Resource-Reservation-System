package com.vityarthi.campus.concurrency;

import java.time.LocalDate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * SlotLockManager provides fine-grained, thread-safe synchronization
 * to eliminate race conditions, double bookings, and deadlocks in the
 * Smart Campus Lab & Resource Reservation System.
 *
 * Utilizes a concurrent registry of ReentrantLocks keyed by
 * [resourceId#date#slotId], guaranteeing mutual exclusion for simultaneous
 * booking attempts on the exact same physical slot while allowing unlimited
 * parallel throughput across independent slots and labs.
 */
public class SlotLockManager {

    private static volatile SlotLockManager instance;
    private final ConcurrentHashMap<String, ReentrantLock> lockRegistry;

    // Concurrency Metrics
    private final AtomicLong totalLockAcquisitions = new AtomicLong(0);
    private final AtomicLong totalCollisionsPrevented = new AtomicLong(0);

    private SlotLockManager() {
        this.lockRegistry = new ConcurrentHashMap<>();
    }

    /**
     * Singleton accessor with thread-safe double-checked locking.
     */
    public static SlotLockManager getInstance() {
        if (instance == null) {
            synchronized (SlotLockManager.class) {
                if (instance == null) {
                    instance = new SlotLockManager();
                }
            }
        }
        return instance;
    }

    private String generateKey(String resourceId, LocalDate date, String slotId) {
        return resourceId + "#" + date.toString() + "#" + slotId;
    }

    /**
     * Attempts to acquire the exclusive lock for a specific slot within timeout.
     *
     * @param resourceId Target lab or equipment ID
     * @param date       Date of reservation
     * @param slotId     TimeSlot ID (e.g. SLOT-1)
     * @param timeoutMs  Maximum wait duration in milliseconds
     * @return true if lock was acquired; false if another thread held the lock
     * @throws InterruptedException if interrupted while waiting
     */
    public boolean tryAcquireSlotLock(String resourceId, LocalDate date, String slotId, long timeoutMs)
            throws InterruptedException {
        String key = generateKey(resourceId, date, slotId);
        ReentrantLock lock = lockRegistry.computeIfAbsent(key, k -> new ReentrantLock(true)); // Fair locking

        boolean acquired = lock.tryLock(timeoutMs, TimeUnit.MILLISECONDS);
        if (acquired) {
            totalLockAcquisitions.incrementAndGet();
        } else {
            totalCollisionsPrevented.incrementAndGet();
        }
        return acquired;
    }

    /**
     * Releases the exclusive lock for the specified slot.
     */
    public void releaseSlotLock(String resourceId, LocalDate date, String slotId) {
        String key = generateKey(resourceId, date, slotId);
        ReentrantLock lock = lockRegistry.get(key);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
            // Prune map entry if lock is completely idle to conserve memory
            if (!lock.hasQueuedThreads() && !lock.isLocked()) {
                lockRegistry.remove(key, lock);
            }
        }
    }

    public long getTotalLockAcquisitions() {
        return totalLockAcquisitions.get();
    }

    public long getTotalCollisionsPrevented() {
        return totalCollisionsPrevented.get();
    }

    public int getActiveLocksCount() {
        return lockRegistry.size();
    }

    public void resetMetrics() {
        totalLockAcquisitions.set(0);
        totalCollisionsPrevented.set(0);
        lockRegistry.clear();
    }
}
