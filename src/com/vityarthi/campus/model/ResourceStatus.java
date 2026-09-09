package com.vityarthi.campus.model;

/**
 * Operational status states for physical campus resources.
 */
public enum ResourceStatus {
    AVAILABLE("Available for Booking"),
    MAINTENANCE("Under Scheduled Maintenance"),
    DECOMMISSIONED("Offline / Decommissioned");

    private final String description;

    ResourceStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
