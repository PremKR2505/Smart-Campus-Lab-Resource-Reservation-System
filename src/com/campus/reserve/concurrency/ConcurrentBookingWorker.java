package com.campus.reserve.concurrency;

import com.campus.reserve.model.User;
import com.campus.reserve.service.ReservationService;

import java.time.LocalDate;
import java.util.concurrent.Callable;

/**
 * ConcurrentBookingWorker executes a simulated parallel reservation request
 * inside an ExecutorService thread pool to test race condition resilience.
 */
public class ConcurrentBookingWorker implements Callable<ReservationResult> {

    private final ReservationService reservationService;
    private final User user;
    private final String resourceId;
    private final LocalDate date;
    private final String slotId;
    private final String purpose;

    public ConcurrentBookingWorker(ReservationService reservationService, User user,
                                   String resourceId, LocalDate date, String slotId, String purpose) {
        this.reservationService = reservationService;
        this.user = user;
        this.resourceId = resourceId;
        this.date = date;
        this.slotId = slotId;
        this.purpose = purpose;
    }

    @Override
    public ReservationResult call() {
        return reservationService.makeReservation(user, resourceId, date, slotId, purpose);
    }
}
