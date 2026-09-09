package com.vityarthi.campus.model;

import java.io.Serializable;

/**
 * Abstract Resource base class representing facilities, labs, and equipment
 * managed within the VIT campus ecosystem.
 */
public abstract class Resource implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String resourceId;
    protected String name;
    protected String blockName;
    protected int floorLevel;
    protected int capacity;
    protected ResourceStatus status;

    public Resource(String resourceId, String name, String blockName, int floorLevel, int capacity, ResourceStatus status) {
        this.resourceId = resourceId;
        this.name = name;
        this.blockName = blockName;
        this.floorLevel = floorLevel;
        this.capacity = capacity;
        this.status = status;
    }

    // Abstract methods to demonstrate polymorphism in lab vs equipment behavior
    public abstract String getResourceType();
    public abstract String getTechnicalSpecifications();
    public abstract boolean isBookableByStudents();

    public String getResourceId() {
        return resourceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBlockName() {
        return blockName;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    public int getFloorLevel() {
        return floorLevel;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public ResourceStatus getStatus() {
        return status;
    }

    public void setStatus(ResourceStatus status) {
        this.status = status;
    }

    public boolean isAvailable() {
        return this.status == ResourceStatus.AVAILABLE;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s, Floor %d, Cap: %d) - %s",
                getResourceType(), name, blockName, floorLevel, capacity, status);
    }
}
