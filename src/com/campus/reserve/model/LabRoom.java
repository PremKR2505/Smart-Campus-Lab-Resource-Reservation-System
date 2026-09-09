package com.campus.reserve.model;

/**
 * LabRoom represents computing, electronics, and specialized laboratories
 * on campus (e.g., AI Research Lab, High Performance Computing, IoT Systems Lab).
 */
public class LabRoom extends Resource {
    private static final long serialVersionUID = 1L;

    private int workstationCount;
    private boolean hasProjector;
    private boolean hasGpuCluster;
    private String osEnvironment; // e.g. "Ubuntu 22.04 LTS / Windows 11 Dual-Boot"

    public LabRoom(String resourceId, String name, String blockName, int floorLevel,
                   int capacity, ResourceStatus status, int workstationCount,
                   boolean hasProjector, boolean hasGpuCluster, String osEnvironment) {
        super(resourceId, name, blockName, floorLevel, capacity, status);
        this.workstationCount = workstationCount;
        this.hasProjector = hasProjector;
        this.hasGpuCluster = hasGpuCluster;
        this.osEnvironment = osEnvironment;
    }

    @Override
    public String getResourceType() {
        return "Laboratory Room";
    }

    @Override
    public String getTechnicalSpecifications() {
        return String.format("Workstations: %d | OS: %s | GPU Cluster: %s | AV Projector: %s",
                workstationCount, osEnvironment, hasGpuCluster ? "YES (NVIDIA A100)" : "Standard",
                hasProjector ? "Available" : "None");
    }

    @Override
    public boolean isBookableByStudents() {
        // GPU clusters require faculty authorization or research project clearance
        return true;
    }

    public int getWorkstationCount() {
        return workstationCount;
    }

    public boolean isHasProjector() {
        return hasProjector;
    }

    public boolean isHasGpuCluster() {
        return hasGpuCluster;
    }

    public String getOsEnvironment() {
        return osEnvironment;
    }
}
