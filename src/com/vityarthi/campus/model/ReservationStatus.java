package com.vityarthi.campus.model;

/**
 * Lifecycle states of an academic reservation request.
 */
public enum ReservationStatus {
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled by User"),
    COMPLETED("Successfully Completed"),
    REJECTED("Rejected / Overridden");

    private final String display;

    ReservationStatus(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }
}
