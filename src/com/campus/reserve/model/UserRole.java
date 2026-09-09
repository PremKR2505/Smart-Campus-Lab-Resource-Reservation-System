package com.campus.reserve.model;

/**
 * UserRole enumeration defining system access tiers.
 */
public enum UserRole {
    STUDENT("Student", 1),
    FACULTY("Faculty / Researcher", 2),
    LAB_ADMIN("Laboratory Administrator", 3);

    private final String displayName;
    private final int accessLevel;

    UserRole(String displayName, int accessLevel) {
        this.displayName = displayName;
        this.accessLevel = accessLevel;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getAccessLevel() {
        return accessLevel;
    }
}
