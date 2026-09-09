package com.vityarthi.campus.storage;

import com.vityarthi.campus.model.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * FileDataStorage provides a thread-safe persistence implementation
 * backed by structured data files in the local filesystem.
 *
 * Automatically provisions realistic VIT campus laboratories, specialized
 * hardware equipment, and initial student/faculty credentials on first launch.
 */
public class FileDataStorage implements DataStorage {

    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = "data/users.csv";
    private static final String RESOURCES_FILE = "data/resources.csv";
    private static final String RESERVATIONS_FILE = "data/reservations.csv";

    private final Map<String, User> userMap = new ConcurrentHashMap<>();
    private final Map<String, Resource> resourceMap = new ConcurrentHashMap<>();
    private final Map<String, Reservation> reservationMap = new ConcurrentHashMap<>();

    private static final Object fileWriteLock = new Object();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public FileDataStorage() {
        initDirectory();
        reload();
        if (resourceMap.isEmpty()) {
            seedInitialData();
            persistAll();
        }
    }

    private void initDirectory() {
        try {
            Path path = Paths.get(DATA_DIR);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not create data directory: " + e.getMessage());
        }
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(userMap.values());
    }

    @Override
    public User getUserById(String userId) {
        if (userId == null) return null;
        return userMap.get(userId);
    }

    @Override
    public User getUserByEmail(String email) {
        if (email == null) return null;
        for (User u : userMap.values()) {
            if (u.getEmail().equalsIgnoreCase(email.trim())) {
                return u;
            }
        }
        return null;
    }

    @Override
    public void saveUser(User user) {
        userMap.put(user.getUserId(), user);
        persistAll();
    }

    @Override
    public List<Resource> getAllResources() {
        return new ArrayList<>(resourceMap.values());
    }

    @Override
    public Resource getResourceById(String resourceId) {
        if (resourceId == null) return null;
        return resourceMap.get(resourceId);
    }

    @Override
    public void saveResource(Resource resource) {
        resourceMap.put(resource.getResourceId(), resource);
        persistAll();
    }

    @Override
    public List<Reservation> getAllReservations() {
        return new ArrayList<>(reservationMap.values());
    }

    @Override
    public Reservation getReservationById(String reservationId) {
        if (reservationId == null) return null;
        return reservationMap.get(reservationId);
    }

    @Override
    public void saveReservation(Reservation reservation) {
        reservationMap.put(reservation.getReservationId(), reservation);
        persistAll();
    }

    @Override
    public void updateReservation(Reservation reservation) {
        reservationMap.put(reservation.getReservationId(), reservation);
        persistAll();
    }

    @Override
    public List<Reservation> getReservationsByUser(String userId) {
        return reservationMap.values().stream()
                .filter(r -> r.getUserId().equalsIgnoreCase(userId))
                .sorted((a, b) -> b.getReservationDate().compareTo(a.getReservationDate()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Reservation> getReservationsByResourceAndDate(String resourceId, LocalDate date) {
        return reservationMap.values().stream()
                .filter(r -> r.getResourceId().equalsIgnoreCase(resourceId)
                        && r.getReservationDate().equals(date)
                        && r.isActive())
                .collect(Collectors.toList());
    }

    @Override
    public void persistAll() {
        synchronized (fileWriteLock) {
            persistUsers();
            persistResources();
            persistReservations();
        }
    }

    private void persistUsers() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE))) {
            writer.println("role,userId,name,email,passwordHash,param1,param2,param3,param4");
            for (User u : userMap.values()) {
                if (u instanceof Student) {
                    Student s = (Student) u;
                    writer.printf("STUDENT,%s,%s,%s,%s,%s,%s,%d,%.2f%n",
                            escape(s.getUserId()), escape(s.getName()), escape(s.getEmail()),
                            escape(s.getPasswordHash()), escape(s.getRegistrationNumber()),
                            escape(s.getDepartment()), s.getSemester(), s.getCgpa());
                } else if (u instanceof Faculty) {
                    Faculty f = (Faculty) u;
                    writer.printf("FACULTY,%s,%s,%s,%s,%s,%s,%s,N/A%n",
                            escape(f.getUserId()), escape(f.getName()), escape(f.getEmail()),
                            escape(f.getPasswordHash()), escape(f.getEmployeeId()),
                            escape(f.getDepartment()), escape(f.getDesignation()));
                } else if (u instanceof LabAdmin) {
                    LabAdmin a = (LabAdmin) u;
                    writer.printf("LAB_ADMIN,%s,%s,%s,%s,%s,%s,%d,N/A%n",
                            escape(a.getUserId()), escape(a.getName()), escape(a.getEmail()),
                            escape(a.getPasswordHash()), escape(a.getAdminStaffId()),
                            escape(a.getAssignedBlock()), a.getClearanceLevel());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to write users file: " + e.getMessage());
        }
    }

    private void persistResources() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(RESOURCES_FILE))) {
            writer.println("type,resourceId,name,blockName,floorLevel,capacity,status,param1,param2,param3,param4");
            for (Resource r : resourceMap.values()) {
                if (r instanceof LabRoom) {
                    LabRoom l = (LabRoom) r;
                    writer.printf("LAB,%s,%s,%s,%d,%d,%s,%d,%b,%b,%s%n",
                            escape(l.getResourceId()), escape(l.getName()), escape(l.getBlockName()),
                            l.getFloorLevel(), l.getCapacity(), l.getStatus().name(),
                            l.getWorkstationCount(), l.isHasProjector(), l.isHasGpuCluster(),
                            escape(l.getOsEnvironment()));
                } else if (r instanceof Equipment) {
                    Equipment eq = (Equipment) r;
                    writer.printf("EQUIPMENT,%s,%s,%s,%d,%d,%s,%s,%s,%s,%d%n",
                            escape(eq.getResourceId()), escape(eq.getName()), escape(eq.getBlockName()),
                            eq.getFloorLevel(), eq.getCapacity(), eq.getStatus().name(),
                            escape(eq.getEquipmentType()), escape(eq.getSerialNumber()),
                            escape(eq.getManufacturer()), eq.getConditionRating());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to write resources file: " + e.getMessage());
        }
    }

    private void persistReservations() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(RESERVATIONS_FILE))) {
            writer.println("resId,userId,userName,role,resourceId,resourceName,slotId,slotFmt,date,purpose,status");
            for (Reservation res : reservationMap.values()) {
                writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                        escape(res.getReservationId()), escape(res.getUserId()), escape(res.getUserName()),
                        res.getUserRole().name(), escape(res.getResourceId()), escape(res.getResourceName()),
                        escape(res.getTimeSlotId()), escape(res.getTimeSlotFormatted()),
                        res.getReservationDate().format(DATE_FMT), escape(res.getPurpose()),
                        res.getStatus().name());
            }
        } catch (IOException e) {
            System.err.println("Failed to write reservations file: " + e.getMessage());
        }
    }

    @Override
    public void reload() {
        synchronized (fileWriteLock) {
            loadUsers();
            loadResources();
            loadReservations();
        }
    }

    private void loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length < 9) continue;
                String role = unescape(parts[0]);
                String id = unescape(parts[1]);
                String name = unescape(parts[2]);
                String email = unescape(parts[3]);
                String pwd = unescape(parts[4]);

                if ("STUDENT".equalsIgnoreCase(role)) {
                    String regNo = unescape(parts[5]);
                    String dept = unescape(parts[6]);
                    int sem = Integer.parseInt(parts[7].trim());
                    double cgpa = Double.parseDouble(parts[8].trim());
                    userMap.put(id, new Student(id, name, email, pwd, regNo, dept, sem, cgpa));
                } else if ("FACULTY".equalsIgnoreCase(role)) {
                    String empId = unescape(parts[5]);
                    String dept = unescape(parts[6]);
                    String desig = unescape(parts[7]);
                    userMap.put(id, new Faculty(id, name, email, pwd, empId, dept, desig));
                } else if ("LAB_ADMIN".equalsIgnoreCase(role)) {
                    String staffId = unescape(parts[5]);
                    String block = unescape(parts[6]);
                    int clearance = Integer.parseInt(parts[7].trim());
                    userMap.put(id, new LabAdmin(id, name, email, pwd, staffId, block, clearance));
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading users file: " + e.getMessage());
        }
    }

    private void loadResources() {
        File file = new File(RESOURCES_FILE);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length < 11) continue;
                String type = unescape(parts[0]);
                String id = unescape(parts[1]);
                String name = unescape(parts[2]);
                String block = unescape(parts[3]);
                int floor = Integer.parseInt(parts[4].trim());
                int cap = Integer.parseInt(parts[5].trim());
                ResourceStatus st = ResourceStatus.valueOf(parts[6].trim().toUpperCase());

                if ("LAB".equalsIgnoreCase(type)) {
                    int ws = Integer.parseInt(parts[7].trim());
                    boolean proj = Boolean.parseBoolean(parts[8].trim());
                    boolean gpu = Boolean.parseBoolean(parts[9].trim());
                    String os = unescape(parts[10]);
                    resourceMap.put(id, new LabRoom(id, name, block, floor, cap, st, ws, proj, gpu, os));
                } else if ("EQUIPMENT".equalsIgnoreCase(type)) {
                    String eqType = unescape(parts[7]);
                    String serial = unescape(parts[8]);
                    String mfg = unescape(parts[9]);
                    int cond = Integer.parseInt(parts[10].trim());
                    resourceMap.put(id, new Equipment(id, name, block, floor, cap, st, eqType, serial, mfg, cond));
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading resources file: " + e.getMessage());
        }
    }

    private void loadReservations() {
        File file = new File(RESERVATIONS_FILE);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length < 11) continue;
                String resId = unescape(parts[0]);
                String userId = unescape(parts[1]);
                String userName = unescape(parts[2]);
                UserRole role = UserRole.valueOf(parts[3].trim());
                String resourceId = unescape(parts[4]);
                String resName = unescape(parts[5]);
                String slotId = unescape(parts[6]);
                String slotFmt = unescape(parts[7]);
                LocalDate date = LocalDate.parse(parts[8].trim(), DATE_FMT);
                String purpose = unescape(parts[9]);
                ReservationStatus status = ReservationStatus.valueOf(parts[10].trim());

                TimeSlot ts = TimeSlot.findById(slotId);
                if (ts == null) {
                    ts = new TimeSlot(slotId, "Session " + slotId, java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0));
                }
                Reservation res = new Reservation(resId, userId, userName, role, resourceId, resName, ts, date, purpose);
                res.setStatus(status);
                reservationMap.put(resId, res);
            }
        } catch (Exception e) {
            System.err.println("Error reading reservations file: " + e.getMessage());
        }
    }

    private void seedInitialData() {
        // Seed Users
        userMap.put("USR-STD-01", new Student("USR-STD-01", "Rohan Chetty", "rohan.chetty@vitstudent.ac.in",
                "vit@2024", "23BCE10045", "SCSE", 4, 9.4));
        userMap.put("USR-STD-02", new Student("USR-STD-02", "Ananya Sharma", "ananya.sharma@vitstudent.ac.in",
                "vit@2024", "23BCE10210", "SCOPE", 4, 8.9));
        userMap.put("USR-FAC-01", new Faculty("USR-FAC-01", "Dr. Rajesh K.", "rajesh.kumar@vit.ac.in",
                "vit@fac", "EMP-5082", "SCSE", "Associate Professor"));
        userMap.put("USR-ADM-01", new LabAdmin("USR-ADM-01", "Vikram Singh", "vikram.admin@vit.ac.in",
                "vit@admin", "ADM-004", "Academic Block 1", 5));

        // Seed Labs
        resourceMap.put("LAB-SCSE-101", new LabRoom("LAB-SCSE-101", "Advanced Computing Lab I",
                "Academic Block 1 (AB-1)", 1, 60, ResourceStatus.AVAILABLE,
                60, true, false, "Ubuntu 22.04 LTS / Windows 11 Dual-Boot"));

        resourceMap.put("LAB-AI-201", new LabRoom("LAB-AI-201", "NVIDIA Deep Learning & GPU Cluster",
                "Central Computing Center", 2, 35, ResourceStatus.AVAILABLE,
                35, true, true, "Ubuntu 22.04 LTS + CUDA 12.4"));

        resourceMap.put("LAB-IOT-301", new LabRoom("LAB-IOT-301", "IoT & Cyber-Physical Systems Lab",
                "Academic Block 2 (AB-2)", 3, 40, ResourceStatus.AVAILABLE,
                40, true, false, "Linux Debian + Real-time Embedded Toolchains"));

        resourceMap.put("LAB-ROBO-401", new LabRoom("LAB-ROBO-401", "Robotics & Automation Arena",
                "Technology Tower (TT)", 1, 30, ResourceStatus.AVAILABLE,
                30, true, false, "ROS2 Humble / Ubuntu Linux"));

        resourceMap.put("LAB-NET-501", new LabRoom("LAB-NET-501", "Cisco Networking & Security Lab",
                "Academic Block 1 (AB-1)", 4, 50, ResourceStatus.AVAILABLE,
                50, true, false, "Packet Tracer / GNS3 / Wireshark Workstations"));

        // Seed Specialized Equipment
        resourceMap.put("EQ-FPGA-01", new Equipment("EQ-FPGA-01", "Xilinx Artix-7 FPGA Development Kit #1",
                "Academic Block 2 (AB-2)", 3, 1, ResourceStatus.AVAILABLE,
                "FPGA Development Board", "SN-XIL-2024-001", "AMD Xilinx", 5));

        resourceMap.put("EQ-DSO-02", new Equipment("EQ-DSO-02", "Keysight 200MHz Digital Oscilloscope",
                "Academic Block 2 (AB-2)", 3, 1, ResourceStatus.AVAILABLE,
                "Benchtop Measuring Tool", "SN-KEYS-9801", "Keysight Technologies", 5));

        resourceMap.put("EQ-VR-03", new Equipment("EQ-VR-03", "Meta Quest 3 Enterprise VR Testing Rig",
                "Central Computing Center", 2, 1, ResourceStatus.AVAILABLE,
                "Spatial Computing Headset", "SN-META-3301", "Meta Platforms", 4));

        resourceMap.put("EQ-RPI-04", new Equipment("EQ-RPI-04", "Raspberry Pi 5 AI Edge Cluster Box",
                "Academic Block 1 (AB-1)", 1, 1, ResourceStatus.AVAILABLE,
                "Edge Compute Kit", "SN-RPI-5002", "Raspberry Pi Foundation", 5));
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace(",", ";").replace("\n", " ");
    }

    private String unescape(String s) {
        if (s == null) return "";
        return s.trim();
    }
}
