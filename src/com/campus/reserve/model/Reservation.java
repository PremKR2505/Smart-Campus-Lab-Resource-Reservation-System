package com.campus.reserve.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Reservation record mapping a User to a Resource for a specific TimeSlot and Date.
 */
public class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;

    private String reservationId;
    private String userId;
    private String userName;
    private UserRole userRole;
    private String resourceId;
    private String resourceName;
    private String timeSlotId;
    private String timeSlotFormatted;
    private LocalDate reservationDate;
    private String purpose;
    private ReservationStatus status;
    private LocalDateTime createdAt;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Reservation(String reservationId, String userId, String userName, UserRole userRole,
                       String resourceId, String resourceName, TimeSlot timeSlot,
                       LocalDate reservationDate, String purpose) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.userName = userName;
        this.userRole = userRole;
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.timeSlotId = timeSlot.getSlotId();
        this.timeSlotFormatted = timeSlot.getFormattedRange();
        this.reservationDate = reservationDate;
        this.purpose = purpose;
        this.status = ReservationStatus.CONFIRMED;
        this.createdAt = LocalDateTime.now();
    }

    public String getReservationId() {
        return reservationId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public UserRole getUserRole() {
        return userRole;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getTimeSlotId() {
        return timeSlotId;
    }

    public String getTimeSlotFormatted() {
        return timeSlotFormatted;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public String getPurpose() {
        return purpose;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isActive() {
        return this.status == ReservationStatus.CONFIRMED;
    }

    @Override
    public String toString() {
        return String.format("[%s] ResID: %s | %s on %s (%s) | Booked by %s (%s) | Purpose: %s",
                status, reservationId, resourceName, reservationDate.format(DATE_FMT),
                timeSlotFormatted, userName, userRole, purpose);
    }
}
