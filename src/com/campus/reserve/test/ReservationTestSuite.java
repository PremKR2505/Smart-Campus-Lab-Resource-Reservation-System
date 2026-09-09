package com.campus.reserve.test;

import com.campus.reserve.concurrency.ConcurrentBookingWorker;
import com.campus.reserve.concurrency.ReservationResult;
import com.campus.reserve.concurrency.SlotLockManager;
import com.campus.reserve.model.*;
import com.campus.reserve.service.*;
import com.campus.reserve.storage.*;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;

/**
 * Comprehensive Automated Test Suite for Smart Campus Lab & Resource Reservation System.
 * Validates OOP architecture, institutional business rules, persistence, and
 * multi-threaded race-condition resolution.
 */
public class ReservationTestSuite {

    private int testsPassed = 0;
    private int testsFailed = 0;

    public static void main(String[] args) {
        new ReservationTestSuite().runAllTests();
    }

    public boolean runAllTests() {
        System.out.println("================================================================");
        System.out.println("   ACADEMIC EVALUATION TEST SUITE: CSE2006 JAVA PROJECT          ");
        System.out.println("   System: Smart Campus Lab & Resource Reservation System        ");
        System.out.println("================================================================\n");

        testOOPPolymorphismAndQuotas();
        testResourceCatalogAndSpecifications();
        testSingleThreadReservationLifecycle();
        testMultiThreadedRaceConditionLocking();

        System.out.println("\n----------------------------------------------------------------");
        System.out.printf("   TEST EXECUTION SUMMARY: %d PASSED, %d FAILED%n", testsPassed, testsFailed);
        System.out.println("----------------------------------------------------------------");

        if (testsFailed == 0) {
            System.out.println("   >>> ALL VERIFICATION GATES PASSED SUCCESSFULLY (100%) <<<");
            return true;
        } else {
            System.err.println("   >>> SOME TESTS FAILED! CHECK OUTPUT ABOVE <<<");
            return false;
        }
    }

    private void assertTrue(String testName, boolean condition, String message) {
        if (condition) {
            System.out.printf("  [PASS] %-45s - %s%n", testName, message);
            testsPassed++;
        } else {
            System.err.printf("  [FAIL] %-45s - %s%n", testName, message);
            testsFailed++;
        }
    }

    private void testOOPPolymorphismAndQuotas() {
        System.out.println(">>> [1/4] Running OOP Polymorphism & Quota Tests...");

        User student = new Student("U1", "Test Student", "s@vit.ac.in", "p", "23BCE1001", "SCSE", 4, 9.1);
        User faculty = new Faculty("U2", "Dr. Prof", "f@vit.ac.in", "p", "EMP-101", "SCSE", "Professor");
        User admin = new LabAdmin("U3", "Admin Staff", "a@vit.ac.in", "p", "ADM-01", "AB-1", 5);

        assertTrue("Student Max Quota", student.getMaxAllowedBookings() == 3, "Student quota must be 3");
        assertTrue("Faculty Max Quota", faculty.getMaxAllowedBookings() == 10, "Faculty quota must be 10");
        assertTrue("Admin Override Check", admin.canOverrideMaintenance(), "Admin can override maintenance");
        assertTrue("Student Override Check", !student.canOverrideMaintenance(), "Student cannot override maintenance");
    }

    private void testResourceCatalogAndSpecifications() {
        System.out.println("\n>>> [2/4] Running Resource Hierarchy & Specs Tests...");

        Resource lab = new LabRoom("L1", "HPC AI Lab", "CCAC", 2, 40, ResourceStatus.AVAILABLE,
                40, true, true, "Ubuntu Linux + CUDA");
        Resource eq = new Equipment("E1", "FPGA Board", "AB-2", 3, 1, ResourceStatus.AVAILABLE,
                "FPGA Kit", "SN-990", "Xilinx", 5);

        assertTrue("LabRoom Polymorphic Type", lab.getResourceType().equals("Laboratory Room"),
                "LabRoom correctly identifies as Laboratory Room");
        assertTrue("Equipment Polymorphic Type", eq.getResourceType().equals("Specialized Equipment"),
                "Equipment correctly identifies as Specialized Equipment");
        assertTrue("Lab GPU Detection", ((LabRoom) lab).isHasGpuCluster(), "GPU capability verified");
    }

    private void testSingleThreadReservationLifecycle() {
        System.out.println("\n>>> [3/4] Running Single-Threaded Booking Lifecycle Tests...");

        DataStorage storage = new FileDataStorage();
        ReservationService resService = new ReservationService(storage);

        User student = new Student("U-TEMP-1", "Test Runner", "test@vit.ac.in", "123",
                "23BCE9999", "SCSE", 4, 9.0);
        LocalDate testDate = LocalDate.now().plusDays(1);
        String slotId = "SLOT-1";

        // Step 1: Initial Booking
        ReservationResult result1 = resService.makeReservation(student, "LAB-AI-201", testDate, slotId, "AI Project Work");
        assertTrue("Initial Booking Success", result1.isSuccess(), "First booking should confirm");

        // Step 2: Attempting duplicate booking on same slot should be rejected
        ReservationResult resultDuplicate = resService.makeReservation(student, "LAB-AI-201", testDate, slotId, "Duplicate Try");
        assertTrue("Duplicate Booking Prevention", !resultDuplicate.isSuccess(),
                "Duplicate booking rejected: " + resultDuplicate.getMessage());

        // Step 3: Cancellation
        if (result1.isSuccess() && result1.getReservation() != null) {
            boolean cancelled = resService.cancelReservation(student, result1.getReservation().getReservationId());
            assertTrue("Reservation Cancellation", cancelled, "Cancellation should succeed and free slot");

            // Step 4: Re-booking after cancellation should succeed
            ReservationResult resultRebook = resService.makeReservation(student, "LAB-AI-201", testDate, slotId, "Re-booked slot");
            assertTrue("Re-booking Freed Slot", resultRebook.isSuccess(), "Slot can be booked after cancellation");

            // Clean up
            if (resultRebook.getReservation() != null) {
                resService.cancelReservation(student, resultRebook.getReservation().getReservationId());
            }
        }
    }

    private void testMultiThreadedRaceConditionLocking() {
        System.out.println("\n>>> [4/4] Running Concurrency & Race-Condition Stress Test (10 Threads)...");

        DataStorage storage = new FileDataStorage();
        ReservationService resService = new ReservationService(storage);
        SlotLockManager lockManager = SlotLockManager.getInstance();
        lockManager.resetMetrics();

        int THREAD_COUNT = 10;
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        LocalDate raceDate = LocalDate.now().plusDays(5);
        String targetSlot = "SLOT-4";
        String targetLab = "LAB-SCSE-101";

        // Pre-clean any test reservation on this target slot from earlier test runs
        List<Reservation> existingBookings = storage.getReservationsByResourceAndDate(targetLab, raceDate);
        User adminOverride = new LabAdmin("SYS", "Admin", "sys@vit.ac.in", "p", "ADM-00", "ALL", 5);
        for (Reservation r : existingBookings) {
            if (r.getTimeSlotId().equalsIgnoreCase(targetSlot)) {
                resService.cancelReservation(adminOverride, r.getReservationId());
            }
        }

        List<Callable<ReservationResult>> tasks = new ArrayList<>();
        for (int i = 1; i <= THREAD_COUNT; i++) {
            User student = new Student("USR-RACE-" + i, "Competitor Student " + i,
                    "student" + i + "@vit.ac.in", "pwd", "23BCE00" + i, "SCSE", 4, 8.5);
            tasks.add(new ConcurrentBookingWorker(resService, student, targetLab, raceDate, targetSlot,
                    "High-priority contest by thread " + i));
        }

        try {
            // Fire all 10 threads simultaneously
            List<Future<ReservationResult>> futures = executor.invokeAll(tasks);
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);

            int successCount = 0;
            int failureCount = 0;

            for (Future<ReservationResult> future : futures) {
                ReservationResult res = future.get();
                if (res.isSuccess()) {
                    successCount++;
                    System.out.println("    [WINNER] " + res.getReservation().getUserName()
                            + " secured reservation " + res.getReservation().getReservationId());
                } else {
                    failureCount++;
                }
            }

            assertTrue("Exact Single Winner Under Race Condition", successCount == 1,
                    String.format("Expected exactly 1 successful booking, got: %d", successCount));

            assertTrue("All Other Contenders Rejected Cleanly", failureCount == (THREAD_COUNT - 1),
                    String.format("Expected %d conflicts prevented, got: %d", (THREAD_COUNT - 1), failureCount));

            assertTrue("Mutex Lock Registry Active", lockManager.getTotalLockAcquisitions() >= 1,
                    "SlotLockManager recorded thread-safe acquisitions");

            System.out.printf("    Concurrency Telemetry: %d Locks Acquired, %d Contention Collisions Prevented.%n",
                    lockManager.getTotalLockAcquisitions(), lockManager.getTotalCollisionsPrevented());

            // Post-clean winner reservation to keep data storage pristine
            for (Future<ReservationResult> future : futures) {
                ReservationResult res = future.get();
                if (res.isSuccess() && res.getReservation() != null) {
                    resService.cancelReservation(adminOverride, res.getReservation().getReservationId());
                }
            }

        } catch (Exception e) {
            assertTrue("Concurrency Stress Test Execution", false, "Exception during thread execution: " + e.getMessage());
        }
    }
}
