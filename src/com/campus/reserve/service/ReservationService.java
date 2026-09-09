package com.campus.reserve.service;

import com.campus.reserve.concurrency.ReservationResult;
import com.campus.reserve.concurrency.SlotLockManager;
import com.campus.reserve.model.*;
import com.campus.reserve.storage.DataStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * ReservationService coordinates the complete transactional booking lifecycle.
 * Integrates SlotLockManager to guarantee absolute thread-safety, prevent double-bookings,
 * and enforce institutional quota limits.
 */
public class ReservationService {

    private final DataStorage storage;
    private final SlotLockManager lockManager;

    public ReservationService(DataStorage storage) {
        this.storage = storage;
        this.lockManager = SlotLockManager.getInstance();
    }

    /**
     * Executes a thread-safe reservation request with fair concurrency locking.
     */
    public ReservationResult makeReservation(User user, String resourceId, LocalDate date,
                                            String slotId, String purpose) {
        long startTime = System.currentTimeMillis();
        String threadName = Thread.currentThread().getName();

        if (user == null) {
            return ReservationResult.failure("Authentication required to reserve resources.",
                    System.currentTimeMillis() - startTime, threadName);
        }

        Resource resource = storage.getResourceById(resourceId);
        if (resource == null) {
            return ReservationResult.failure("Resource not found with ID: " + resourceId,
                    System.currentTimeMillis() - startTime, threadName);
        }

        if (!resource.isAvailable() && !user.canOverrideMaintenance()) {
            return ReservationResult.failure("Resource is currently under maintenance or decommissioned.",
                    System.currentTimeMillis() - startTime, threadName);
        }

        TimeSlot timeSlot = TimeSlot.findById(slotId);
        if (timeSlot == null) {
            return ReservationResult.failure("Invalid TimeSlot specified: " + slotId,
                    System.currentTimeMillis() - startTime, threadName);
        }

        // Enforce user role quota limit
        List<Reservation> userReservations = storage.getReservationsByUser(user.getUserId());
        long activeCount = userReservations.stream().filter(Reservation::isActive).count();
        if (activeCount >= user.getMaxAllowedBookings()) {
            return ReservationResult.failure(
                    String.format("Quota limit reached. %s allowed max %d active reservations.",
                            user.getRole(), user.getMaxAllowedBookings()),
                    System.currentTimeMillis() - startTime, threadName);
        }

        // Acquire fine-grained mutex lock for this specific physical slot
        boolean lockAcquired = false;
        try {
            lockAcquired = lockManager.tryAcquireSlotLock(resourceId, date, slotId, 3000);
            if (!lockAcquired) {
                return ReservationResult.failure(
                        "Lock contention: Another transaction is currently holding or finalizing this slot.",
                        System.currentTimeMillis() - startTime, threadName);
            }

            // Double-check slot occupancy under lock protection (Critical Section)
            List<Reservation> existingActive = storage.getReservationsByResourceAndDate(resourceId, date);
            for (Reservation r : existingActive) {
                if (r.getTimeSlotId().equalsIgnoreCase(slotId)) {
                    return ReservationResult.failure(
                            "Double-booking prevented: Slot " + slotId + " is already confirmed by " + r.getUserName(),
                            System.currentTimeMillis() - startTime, threadName);
                }
            }

            // Ensure the user hasn't booked another concurrent lab during the same time window
            for (Reservation userRes : userReservations) {
                if (userRes.isActive()
                        && userRes.getReservationDate().equals(date)
                        && userRes.getTimeSlotId().equalsIgnoreCase(slotId)) {
                    return ReservationResult.failure(
                            "Schedule conflict: You already have a confirmed booking (" + userRes.getResourceName()
                                    + ") during this same time window.",
                            System.currentTimeMillis() - startTime, threadName);
                }
            }

            // Create and persist confirmed reservation
            String resId = "RES-" + date.toString().replace("-", "") + "-"
                    + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

            Reservation reservation = new Reservation(
                    resId, user.getUserId(), user.getName(), user.getRole(),
                    resource.getResourceId(), resource.getName(),
                    timeSlot, date, (purpose == null || purpose.trim().isEmpty()) ? "Academic Work" : purpose.trim()
            );

            storage.saveReservation(reservation);

            long duration = System.currentTimeMillis() - startTime;
            return ReservationResult.successful("Reservation successfully confirmed!", reservation, duration, threadName);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ReservationResult.failure("Reservation interrupted during lock acquisition.",
                    System.currentTimeMillis() - startTime, threadName);
        } finally {
            if (lockAcquired) {
                lockManager.releaseSlotLock(resourceId, date, slotId);
            }
        }
    }

    /**
     * Cancels an existing active reservation.
     */
    public boolean cancelReservation(User requestingUser, String reservationId) {
        if (requestingUser == null || reservationId == null) return false;

        Reservation res = storage.getReservationById(reservationId);
        if (res == null || !res.isActive()) return false;

        // Permissions: User can cancel their own, or Admin can cancel any
        boolean isOwner = res.getUserId().equalsIgnoreCase(requestingUser.getUserId());
        boolean isAdmin = requestingUser.getRole() == UserRole.LAB_ADMIN;

        if (isOwner || isAdmin) {
            res.setStatus(ReservationStatus.CANCELLED);
            storage.updateReservation(res);
            return true;
        }
        return false;
    }

    public List<Reservation> getUserReservations(String userId) {
        return storage.getReservationsByUser(userId);
    }

    public List<Reservation> getAllReservations() {
        return storage.getAllReservations();
    }
}
