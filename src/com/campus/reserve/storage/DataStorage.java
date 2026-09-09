package com.campus.reserve.storage;

import com.campus.reserve.model.Reservation;
import com.campus.reserve.model.Resource;
import com.campus.reserve.model.User;

import java.time.LocalDate;
import java.util.List;

/**
 * DataStorage contract defining CRUD operations and query capabilities
 * for users, campus resources, and reservation records.
 */
public interface DataStorage {

    // User Operations
    List<User> getAllUsers();
    User getUserById(String userId);
    User getUserByEmail(String email);
    void saveUser(User user);

    // Resource Operations
    List<Resource> getAllResources();
    Resource getResourceById(String resourceId);
    void saveResource(Resource resource);

    // Reservation Operations
    List<Reservation> getAllReservations();
    Reservation getReservationById(String reservationId);
    void saveReservation(Reservation reservation);
    void updateReservation(Reservation reservation);
    List<Reservation> getReservationsByUser(String userId);
    List<Reservation> getReservationsByResourceAndDate(String resourceId, LocalDate date);

    // Persistence Lifecycle
    void persistAll();
    void reload();
}
