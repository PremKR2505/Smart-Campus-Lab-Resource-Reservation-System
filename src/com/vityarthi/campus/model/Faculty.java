package com.vityarthi.campus.model;

import com.vityarthi.campus.config.CampusConfig;

/**
 * Faculty domain model extending User.
 * Faculty members receive elevated booking limits and priority resource allocation.
 */
public class Faculty extends User {
    private static final long serialVersionUID = 1L;

    private String employeeId;    // e.g. EMP5042
    private String department;    // e.g. SCSE
    private String designation;   // e.g. Associate Professor
    private boolean priorityAccess;

    public Faculty(String userId, String name, String email, String passwordHash,
                   String employeeId, String department, String designation) {
        super(userId, name, email, passwordHash, UserRole.FACULTY);
        this.employeeId = employeeId;
        this.department = department;
        this.designation = designation;
        this.priorityAccess = true;
    }

    @Override
    public int getMaxAllowedBookings() {
        return CampusConfig.FACULTY_MAX_CONCURRENT_RESERVATIONS;
    }

    @Override
    public boolean canOverrideMaintenance() {
        return false;
    }

    @Override
    public String getInstitutionalIdentifier() {
        return "EmpID: " + employeeId + " (" + designation + ")";
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getDepartment() {
        return department;
    }

    public String getDesignation() {
        return designation;
    }

    public boolean isPriorityAccess() {
        return priorityAccess;
    }
}
