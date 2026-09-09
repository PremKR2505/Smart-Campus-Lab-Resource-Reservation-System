package com.vityarthi.campus.model;

import com.vityarthi.campus.config.CampusConfig;

/**
 * LabAdmin domain model extending User.
 * System operators with facility management rights, maintenance toggle, and audit inspection.
 */
public class LabAdmin extends User {
    private static final long serialVersionUID = 1L;

    private String adminStaffId;   // e.g. ADM-902
    private String assignedBlock;  // e.g. AB-1 / CCAC
    private int clearanceLevel;    // 1 to 5

    public LabAdmin(String userId, String name, String email, String passwordHash,
                    String adminStaffId, String assignedBlock, int clearanceLevel) {
        super(userId, name, email, passwordHash, UserRole.LAB_ADMIN);
        this.adminStaffId = adminStaffId;
        this.assignedBlock = assignedBlock;
        this.clearanceLevel = clearanceLevel;
    }

    @Override
    public int getMaxAllowedBookings() {
        return CampusConfig.LAB_ADMIN_MAX_CONCURRENT_RESERVATIONS;
    }

    @Override
    public boolean canOverrideMaintenance() {
        return true;
    }

    @Override
    public String getInstitutionalIdentifier() {
        return "AdminStaff: " + adminStaffId + " [Block: " + assignedBlock + "]";
    }

    public String getAdminStaffId() {
        return adminStaffId;
    }

    public String getAssignedBlock() {
        return assignedBlock;
    }

    public void setAssignedBlock(String assignedBlock) {
        this.assignedBlock = assignedBlock;
    }

    public int getClearanceLevel() {
        return clearanceLevel;
    }
}
