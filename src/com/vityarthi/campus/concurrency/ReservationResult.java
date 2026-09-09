package com.vityarthi.campus.concurrency;

import com.vityarthi.campus.model.Reservation;

/**
 * Encapsulates the outcome of a reservation attempt, including concurrency telemetry.
 */
public class ReservationResult {

    private final boolean success;
    private final String message;
    private final Reservation reservation;
    private final long executionDurationMs;
    private final String threadName;

    public ReservationResult(boolean success, String message, Reservation reservation,
                             long executionDurationMs, String threadName) {
        this.success = success;
        this.message = message;
        this.reservation = reservation;
        this.executionDurationMs = executionDurationMs;
        this.threadName = threadName;
    }

    public static ReservationResult successful(String message, Reservation reservation,
                                              long durationMs, String threadName) {
        return new ReservationResult(true, message, reservation, durationMs, threadName);
    }

    public static ReservationResult failure(String message, long durationMs, String threadName) {
        return new ReservationResult(false, message, null, durationMs, threadName);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public long getExecutionDurationMs() {
        return executionDurationMs;
    }

    public String getThreadName() {
        return threadName;
    }

    @Override
    public String toString() {
        return String.format("[%s] Thread: %s | Duration: %dms | Status: %s | %s",
                success ? "SUCCESS" : "FAILED", threadName, executionDurationMs,
                reservation != null ? reservation.getReservationId() : "NONE", message);
    }
}
