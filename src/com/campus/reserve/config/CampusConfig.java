package com.campus.reserve.config;

/**
 * CampusConfig
 * Holds institutional settings, campus infrastructure blocks, and operational defaults
 * for the Smart Campus Lab & Resource Reservation System at VIT Bhopal University.
 */
public class CampusConfig {

    public static final String INSTITUTION_NAME = "VIT Bhopal University";
    public static final String SYSTEM_NAME = "Smart Campus Lab & Resource Reservation System";
    public static final String SYSTEM_VERSION = "v2.4.0-LTS";
    public static final String SYSTEM_TAGLINE = "High-Throughput Academic Lab & Equipment Booking Platform";

    // Campus location
    private static String activeCampus = "VIT Bhopal University (Kothri Kalan, Sehore)";

    // Official VIT Bhopal Campus Academic Blocks & Facilities
    public static final String[] CAMPUS_BLOCKS = {
        "Academic Block (AB) - Computing & Cyber Wing",
        "Lab Complex (LC) - Advanced Engineering Labs",
        "AI & Gaming Studio (Central Block)",
        "IoT & Robotics Innovation Center",
        "SEEE Hardware & Microelectronics Lab"
    };

    // Official VIT Bhopal Schools & Divisions
    public static final String[] DEPARTMENTS = {
        "SCSE - School of Computing Science and Engineering",
        "SEEE - School of Electrical and Electronics Engineering",
        "SMEC - School of Mechanical Engineering",
        "SASL - School of Advanced Sciences and Languages",
        "School of Bioengineering"
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
