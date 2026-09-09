package com.vityarthi.campus.model;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Immutable TimeSlot representing standardized campus lab operational periods.
 */
public class TimeSlot implements Serializable, Comparable<TimeSlot> {
    private static final long serialVersionUID = 1L;

    private final String slotId;       // e.g. "SLOT-1", "SLOT-2"
    private final String label;        // e.g. "Morning Session A"
    private final LocalTime startTime; // e.g. 08:00
    private final LocalTime endTime;   // e.g. 10:00

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public TimeSlot(String slotId, String label, LocalTime startTime, LocalTime endTime) {
        this.slotId = slotId;
        this.label = label;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getSlotId() {
        return slotId;
    }

    public String getLabel() {
        return label;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getFormattedRange() {
        return startTime.format(TIME_FORMAT) + " - " + endTime.format(TIME_FORMAT);
    }

    /**
     * Standard academic daily schedule slots
     */
    public static List<TimeSlot> getStandardCampusSlots() {
        List<TimeSlot> slots = new ArrayList<>();
        slots.add(new TimeSlot("SLOT-1", "Morning Session 1", LocalTime.of(8, 0), LocalTime.of(10, 0)));
        slots.add(new TimeSlot("SLOT-2", "Morning Session 2", LocalTime.of(10, 15), LocalTime.of(12, 15)));
        slots.add(new TimeSlot("SLOT-3", "Afternoon Session 1", LocalTime.of(13, 0), LocalTime.of(15, 0)));
        slots.add(new TimeSlot("SLOT-4", "Afternoon Session 2", LocalTime.of(15, 15), LocalTime.of(17, 15)));
        slots.add(new TimeSlot("SLOT-5", "Evening Research", LocalTime.of(17, 30), LocalTime.of(19, 30)));
        return slots;
    }

    public static TimeSlot findById(String slotId) {
        for (TimeSlot s : getStandardCampusSlots()) {
            if (s.getSlotId().equalsIgnoreCase(slotId)) {
                return s;
            }
        }
        return null;
    }

    @Override
    public int compareTo(TimeSlot other) {
        return this.startTime.compareTo(other.startTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSlot timeSlot = (TimeSlot) o;
        return Objects.equals(slotId, timeSlot.slotId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(slotId);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s)", slotId, label, getFormattedRange());
    }
}
