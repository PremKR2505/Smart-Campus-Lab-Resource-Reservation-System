package com.vityarthi.campus.service;

import com.vityarthi.campus.model.User;
import com.vityarthi.campus.model.UserRole;
import com.vityarthi.campus.storage.DataStorage;

/**
 * AuthService manages credential verification, active session tracking,
 * and role-based authorization for the reservation system.
 */
public class AuthService {

    private final DataStorage storage;
    private User currentUser;

    public AuthService(DataStorage storage) {
        this.storage = storage;
    }

    /**
     * Authenticates user against stored credentials.
     */
    public synchronized boolean login(String emailOrId, String password) {
        if (emailOrId == null || password == null) return false;

        User user = storage.getUserByEmail(emailOrId.trim());
        if (user == null) {
            user = storage.getUserById(emailOrId.trim());
        }

        if (user != null && user.getPasswordHash().equals(password.trim())) {
            this.currentUser = user;
            return true;
        }
        return false;
    }

    public synchronized void logout() {
        this.currentUser = null;
    }

    public synchronized User getCurrentUser() {
        return currentUser;
    }

    public synchronized boolean isLoggedIn() {
        return currentUser != null;
    }

    public synchronized boolean hasRole(UserRole role) {
        return currentUser != null && currentUser.getRole() == role;
    }

    public synchronized boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == UserRole.LAB_ADMIN;
    }

    public synchronized void setCurrentUser(User user) {
        this.currentUser = user;
    }
}
