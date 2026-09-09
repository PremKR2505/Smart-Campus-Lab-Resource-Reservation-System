package com.vityarthi.campus.service;

import com.vityarthi.campus.concurrency.SlotLockManager;
import com.vityarthi.campus.model.Reservation;
import com.vityarthi.campus.model.Resource;
import com.vityarthi.campus.model.UserRole;
import com.vityarthi.campus.storage.DataStorage;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AnalyticsService computes operational intelligence, utilization metrics,
 * and concurrency efficiency statistics for campus administration.
 */
public class AnalyticsService {

    private final DataStorage storage;
    private final SlotLockManager lockManager;

    public AnalyticsService(DataStorage storage) {
        this.storage = storage;
        this.lockManager = SlotLockManager.getInstance();
    }

    public Map<String, Object> getSystemAnalytics() {
        Map<String, Object> metrics = new LinkedHashMap<>();

        List<Resource> resources = storage.getAllResources();
        List<Reservation> reservations = storage.getAllReservations();

        long totalBookings = reservations.size();
        long activeBookings = reservations.stream().filter(Reservation::isActive).count();
        long cancelledBookings = reservations.stream()
                .filter(r -> r.getStatus() == com.vityarthi.campus.model.ReservationStatus.CANCELLED)
                .count();

        // Role Breakdown
        Map<UserRole, Long> roleDistribution = reservations.stream()
                .collect(Collectors.groupingBy(Reservation::getUserRole, Collectors.counting()));

        // Resource Popularity
        Map<String, Long> resourcePopularity = reservations.stream()
                .collect(Collectors.groupingBy(Reservation::getResourceName, Collectors.counting()));

        List<Map.Entry<String, Long>> topResources = resourcePopularity.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .collect(Collectors.toList());

        metrics.put("totalResources", resources.size());
        metrics.put("totalBookings", totalBookings);
        metrics.put("activeBookings", activeBookings);
        metrics.put("cancelledBookings", cancelledBookings);
        metrics.put("roleDistribution", roleDistribution);
        metrics.put("topResources", topResources);
        metrics.put("lockAcquisitions", lockManager.getTotalLockAcquisitions());
        metrics.put("collisionsPrevented", lockManager.getTotalCollisionsPrevented());

        return metrics;
    }
}
