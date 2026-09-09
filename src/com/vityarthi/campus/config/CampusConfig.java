package com.vityarthi.campus.config;

/**
 * CampusConfig
 * Holds campus environment properties, institutional settings, and defaults
 * for the Smart Campus Lab & Resource Reservation System.
 * Tailored for VIT campus infrastructure with configurable presets.
 */
public class CampusConfig {

    public static final String INSTITUTION_NAME = "Vellore Institute of Technology (VIT)";
    public static final String SYSTEM_NAME = "Smart Campus Lab & Resource Reservation System";
    public static final String SYSTEM_VERSION = "v2.4.0-LTS";
    public static final String SYSTEM_TAGLINE = "High-Throughput Concurrent Academic Lab & Equipment Booking Platform";

    // Configurable active campus preset
    private static String activeCampus = "VIT Campus (Customizable / Bhopal / Vellore / Chennai / AP)";

    // Default Academic Blocks
    public static final String[] CAMPUS_BLOCKS = {
        "Academic Block 1 (AB-1) - Computing Wing",
        "Academic Block 2 (AB-2) - Innovation Wing",
        "Technology Tower (TT) - Advanced Research",
        "Central Computing & AI Cluster (CCAC)",
        "IoT & Embedded Hardware Complex"
    };

    // Department / School names
    public static final String[] DEPARTMENTS = {
        "SCSE - School of Computer Science and Engineering",
        "SCOPE - School of Computer Science & Operations",
        "SENSE - School of Electronics Engineering",
        "SITE - School of Information Technology & Engineering",
        "SELECT - School of Electrical Engineering"
    };

    // Quotas and operational rules
    public static final int STUDENT_MAX_CONCURRENT_RESERVATIONS = 3;
    public static final int FACULTY_MAX_CONCURRENT_RESERVATIONS = 10;
    public static final int LAB_ADMIN_MAX_CONCURRENT_RESERVATIONS = 99;

    public static final int CAMPUS_OPEN_HOUR = 8;   // 08:00 AM
    public static final int CAMPUS_CLOSE_HOUR = 20; // 08:00 PM

    public static synchronized String getActiveCampus() {
        return activeCampus;
    }

    public static synchronized void setActiveCampus(String campusName) {
        if (campusName != null && !campusName.trim().isEmpty()) {
            activeCampus = campusName.trim();
        }
    }
}
