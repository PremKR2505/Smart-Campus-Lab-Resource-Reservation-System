package com.campus.reserve.model;

import com.campus.reserve.config.CampusConfig;

/**
 * Student domain model extending User.
 * Represents enrolled students with registration number, school, and semester.
 */
public class Student extends User {
    private static final long serialVersionUID = 1L;

    private String registrationNumber; // e.g. 23BCE10045
    private String department;         // e.g. SCSE
    private int semester;              // e.g. 4
    private double cgpa;

    public Student(String userId, String name, String email, String passwordHash,
                   String registrationNumber, String department, int semester, double cgpa) {
        super(userId, name, email, passwordHash, UserRole.STUDENT);
        this.registrationNumber = registrationNumber;
        this.department = department;
        this.semester = semester;
        this.cgpa = cgpa;
    }

    @Override
    public int getMaxAllowedBookings() {
        return CampusConfig.STUDENT_MAX_CONCURRENT_RESERVATIONS;
    }

    @Override
    public boolean canOverrideMaintenance() {
        return false;
    }

    @Override
    public String getInstitutionalIdentifier() {
        return "RegNo: " + registrationNumber;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public double getCgpa() {
        return cgpa;
    }

    public void setCgpa(double cgpa) {
        this.cgpa = cgpa;
    }
}
