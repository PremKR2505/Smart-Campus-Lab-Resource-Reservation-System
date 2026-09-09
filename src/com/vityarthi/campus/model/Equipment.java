package com.vityarthi.campus.model;

/**
 * Equipment domain model representing portable and specialized instruments
 * (e.g., Digital Storage Oscilloscopes, FPGA Development Boards, VR Headsets).
 */
public class Equipment extends Resource {
    private static final long serialVersionUID = 1L;

    private String equipmentType;     // e.g. "FPGA Kit", "DSO", "VR Headset"
    private String serialNumber;      // e.g. "SN-XILINX-2024-09"
    private String manufacturer;      // e.g. "Xilinx / AMD", "Keysight"
    private int conditionRating;      // 1 (Poor) to 5 (Pristine)

    public Equipment(String resourceId, String name, String blockName, int floorLevel,
                     int capacity, ResourceStatus status, String equipmentType,
                     String serialNumber, String manufacturer, int conditionRating) {
        super(resourceId, name, blockName, floorLevel, capacity, status);
        this.equipmentType = equipmentType;
        this.serialNumber = serialNumber;
        this.manufacturer = manufacturer;
        this.conditionRating = conditionRating;
    }

    @Override
    public String getResourceType() {
        return "Specialized Equipment";
    }

    @Override
    public String getTechnicalSpecifications() {
        return String.format("Type: %s | S/N: %s | Brand: %s | Condition: %d/5 Stars",
                equipmentType, serialNumber, manufacturer, conditionRating);
    }

    @Override
    public boolean isBookableByStudents() {
        return true;
    }

    public String getEquipmentType() {
        return equipmentType;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public int getConditionRating() {
        return conditionRating;
    }
}
