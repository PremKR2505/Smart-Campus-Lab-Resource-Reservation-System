package com.vityarthi.campus.service;

import com.vityarthi.campus.model.*;
import com.vityarthi.campus.storage.DataStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ResourceService provides resource discovery, filtering, catalog inspection,
 * and time-slot schedule availability calculations.
 */
public class ResourceService {

    private final DataStorage storage;

    public ResourceService(DataStorage storage) {
        this.storage = storage;
    }

    public List<Resource> getAllResources() {
        return storage.getAllResources();
    }

    public List<LabRoom> getAllLabs() {
        return storage.getAllResources().stream()
                .filter(r -> r instanceof LabRoom)
                .map(r -> (LabRoom) r)
                .collect(Collectors.toList());
    }

    public List<Equipment> getAllEquipment() {
        return storage.getAllResources().stream()
                .filter(r -> r instanceof Equipment)
                .map(r -> (Equipment) r)
                .collect(Collectors.toList());
    }

    public Resource getResourceById(String resourceId) {
        return storage.getResourceById(resourceId);
    }

    public List<Resource> filterByBlock(String blockName) {
        if (blockName == null || blockName.trim().isEmpty()) {
            return getAllResources();
        }
        return storage.getAllResources().stream()
                .filter(r -> r.getBlockName().toLowerCase().contains(blockName.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Inspects a resource's availability across all standard academic slots on a given date.
     * Returns a map of TimeSlot -> Boolean (true = Available, false = Booked/Maintenance).
     */
    public Map<TimeSlot, Boolean> getSlotAvailability(String resourceId, LocalDate date) {
        Map<TimeSlot, Boolean> availabilityMap = new LinkedHashMap<>();
        Resource resource = storage.getResourceById(resourceId);
        List<TimeSlot> standardSlots = TimeSlot.getStandardCampusSlots();

        if (resource == null || !resource.isAvailable()) {
            for (TimeSlot slot : standardSlots) {
                availabilityMap.put(slot, false);
            }
            return availabilityMap;
        }

        List<Reservation> activeReservations = storage.getReservationsByResourceAndDate(resourceId, date);
        Set<String> bookedSlotIds = activeReservations.stream()
                .map(Reservation::getTimeSlotId)
                .collect(Collectors.toSet());

        for (TimeSlot slot : standardSlots) {
            availabilityMap.put(slot, !bookedSlotIds.contains(slot.getSlotId()));
        }

        return availabilityMap;
    }

    /**
     * Toggles maintenance status for administrative work.
     */
    public boolean updateResourceStatus(String resourceId, ResourceStatus newStatus) {
        Resource res = storage.getResourceById(resourceId);
        if (res != null) {
            res.setStatus(newStatus);
            storage.saveResource(res);
            return true;
        }
        return false;
    }
}
