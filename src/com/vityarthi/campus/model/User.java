package com.vityarthi.campus.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Abstract User base class establishing core identity, authentication,
 * and polymorphic role attributes.
 */
public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String userId;
    protected String name;
    protected String email;
    protected String passwordHash;
    protected UserRole role;
    protected LocalDateTime registeredAt;

    public User(String userId, String name, String email, String passwordHash, UserRole role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.registeredAt = LocalDateTime.now();
    }

    // Abstract polymorphic methods to be implemented by Student, Faculty, and LabAdmin
    public abstract int getMaxAllowedBookings();
    public abstract boolean canOverrideMaintenance();
    public abstract String getInstitutionalIdentifier();

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s) - %s", role, name, getInstitutionalIdentifier(), email);
    }
}
