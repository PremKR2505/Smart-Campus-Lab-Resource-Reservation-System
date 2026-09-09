package com.vityarthi.campus.ui;

import com.vityarthi.campus.concurrency.ReservationResult;
import com.vityarthi.campus.concurrency.SlotLockManager;
import com.vityarthi.campus.config.CampusConfig;
import com.vityarthi.campus.model.*;
import com.vityarthi.campus.service.*;
import com.vityarthi.campus.storage.DataStorage;
import com.vityarthi.campus.test.ReservationTestSuite;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * ConsoleUI provides an interactive, color-formatted terminal interface
 * for the Smart Campus Lab & Resource Reservation System.
 */
public class ConsoleUI {

    private final DataStorage storage;
    private final AuthService authService;
    private final ResourceService resourceService;
    private final ReservationService reservationService;
    private final AnalyticsService analyticsService;
    private final Scanner scanner;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ANSI Colors for formatted terminal rendering
    private static final String RESET = "\u001B[0m";
    private static final String CYAN = "\u001B[36m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String BOLD = "\u001B[1m";

    public ConsoleUI(DataStorage storage, AuthService authService,
                     ResourceService resourceService, ReservationService reservationService,
                     AnalyticsService analyticsService) {
        this.storage = storage;
        this.authService = authService;
        this.resourceService = resourceService;
        this.reservationService = reservationService;
        this.analyticsService = analyticsService;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        // Default login as Student on start for quick demo
        if (!authService.isLoggedIn()) {
            authService.login("USR-STD-01", "vit@2024");
        }

        boolean running = true;
        while (running) {
            printHeader();
            printMenu();
            System.out.print(CYAN + "Select an option [0-9]: " + RESET);
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    displayLabs();
                    break;
                case "2":
                    displayEquipment();
                    break;
                case "3":
                    checkAvailabilityMatrix();
                    break;
                case "4":
                    bookResourceFlow();
                    break;
                case "5":
                    viewAndCancelMyBookings();
                    break;
                case "6":
                    showAnalyticsDashboard();
                    break;
                case "7":
                    adminMaintenanceControl();
                    break;
                case "8":
                    switchUserFlow();
                    break;
                case "9":
                    runLiveConcurrencyTest();
                    break;
                case "0":
                    System.out.println(GREEN + "\nThank you for using " + CampusConfig.SYSTEM_NAME + ". Goodbye!" + RESET);
                    running = false;
                    break;
                default:
                    System.out.println(RED + "Invalid option! Please enter a number between 0 and 9." + RESET);
            }
            if (running) {
                System.out.println("\nPress [ENTER] to return to Main Menu...");
                scanner.nextLine();
            }
        }
    }

    private void printHeader() {
        System.out.println("\n" + CYAN + BOLD + "==========================================================================================" + RESET);
        System.out.println(CYAN + BOLD + "           " + CampusConfig.INSTITUTION_NAME.toUpperCase() + RESET);
        System.out.println(CYAN + BOLD + "           " + CampusConfig.SYSTEM_NAME.toUpperCase() + " (" + CampusConfig.SYSTEM_VERSION + ")" + RESET);
        System.out.println(YELLOW + "           Campus: " + CampusConfig.getActiveCampus() + RESET);
        System.out.println(CYAN + BOLD + "==========================================================================================" + RESET);

        if (authService.isLoggedIn()) {
            User u = authService.getCurrentUser();
            System.out.printf(GREEN + " [Logged In] %s | %s | Role: %s | Active Quota Limit: %d Bookings%n" + RESET,
                    u.getName(), u.getInstitutionalIdentifier(), u.getRole(), u.getMaxAllowedBookings());
        } else {
            System.out.println(YELLOW + " [Guest Mode] Not Logged In" + RESET);
        }
        System.out.println("------------------------------------------------------------------------------------------");
    }

    private void printMenu() {
        System.out.println("  1. Browse Campus Laboratories & Workstations");
        System.out.println("  2. Browse Specialized Hardware & Equipment");
        System.out.println("  3. View Real-Time Slot Availability Schedule");
        System.out.println("  4. Reserve a Lab Slot or Equipment");
        System.out.println("  5. View My Active Reservations & Cancel Booking");
        System.out.println("  6. Institutional Utilization & Analytics Dashboard");
        System.out.println("  7. Administrative Facility & Maintenance Control (Admin)");
        System.out.println("  8. Switch User Account (Student / Faculty / Admin)");
        System.out.println("  9. Run Real-Time Concurrency Race-Condition Stress Test");
        System.out.println("  0. Exit Application");
        System.out.println("------------------------------------------------------------------------------------------");
    }

    private void displayLabs() {
        System.out.println(BOLD + "\n>>> CAMPUS LABORATORIES CATALOG:" + RESET);
        List<LabRoom> labs = resourceService.getAllLabs();
        System.out.printf("%-15s %-32s %-25s %-6s %-12s%n", "RESOURCE ID", "LABORATORY NAME", "LOCATION BLOCK", "CAP", "STATUS");
        System.out.println("----------------------------------------------------------------------------------------------------");
        for (LabRoom l : labs) {
            String statusColor = l.isAvailable() ? GREEN : RED;
            System.out.printf("%-15s %-32s %-25s %-6d " + statusColor + "%-12s" + RESET + "%n",
                    l.getResourceId(), l.getName(), l.getBlockName(), l.getCapacity(), l.getStatus().name());
            System.out.println("   └─ Technical Specs: " + l.getTechnicalSpecifications());
        }
    }

    private void displayEquipment() {
        System.out.println(BOLD + "\n>>> SPECIALIZED RESEARCH EQUIPMENT CATALOG:" + RESET);
        List<Equipment> equipment = resourceService.getAllEquipment();
        System.out.printf("%-15s %-35s %-22s %-15s %-10s%n", "EQUIPMENT ID", "NAME", "LOCATION", "TYPE", "STATUS");
        System.out.println("----------------------------------------------------------------------------------------------------");
        for (Equipment eq : equipment) {
            String statusColor = eq.isAvailable() ? GREEN : RED;
            System.out.printf("%-15s %-35s %-22s %-15s " + statusColor + "%-10s" + RESET + "%n",
                    eq.getResourceId(), eq.getName(), eq.getBlockName(), eq.getEquipmentType(), eq.getStatus().name());
            System.out.println("   └─ Hardware Specs: " + eq.getTechnicalSpecifications());
        }
    }

    private void checkAvailabilityMatrix() {
        System.out.print("\nEnter Resource ID (e.g., LAB-SCSE-101, LAB-AI-201, EQ-FPGA-01): ");
        String resId = scanner.nextLine().trim();
        Resource res = resourceService.getResourceById(resId);
        if (res == null) {
            System.out.println(RED + "Resource not found!" + RESET);
            return;
        }

        LocalDate date = getTargetDateFromUser();
        Map<TimeSlot, Boolean> schedule = resourceService.getSlotAvailability(res.getResourceId(), date);

        System.out.println(BOLD + "\nAvailability Schedule for " + res.getName() + " on " + date.format(DATE_FMT) + ":" + RESET);
        System.out.printf("%-10s %-25s %-20s %-15s%n", "SLOT ID", "SESSION LABEL", "TIME WINDOW", "STATUS");
        System.out.println("-------------------------------------------------------------------------");
        for (Map.Entry<TimeSlot, Boolean> entry : schedule.entrySet()) {
            TimeSlot slot = entry.getKey();
            boolean isFree = entry.getValue();
            String status = isFree ? (GREEN + "AVAILABLE" + RESET) : (RED + "RESERVED" + RESET);
            System.out.printf("%-10s %-25s %-20s %s%n",
                    slot.getSlotId(), slot.getLabel(), slot.getFormattedRange(), status);
        }
    }

    private void bookResourceFlow() {
        if (!authService.isLoggedIn()) {
            System.out.println(RED + "Please log in first (Option 8) to reserve resources!" + RESET);
            return;
        }

        System.out.print("\nEnter Resource ID to reserve (e.g., LAB-SCSE-101): ");
        String resId = scanner.nextLine().trim();
        Resource res = resourceService.getResourceById(resId);
        if (res == null) {
            System.out.println(RED + "Invalid Resource ID." + RESET);
            return;
        }

        LocalDate date = getTargetDateFromUser();

        System.out.println("\nStandard Academic Time Slots:");
        for (TimeSlot ts : TimeSlot.getStandardCampusSlots()) {
            System.out.printf(" [%s] %s (%s)%n", ts.getSlotId(), ts.getLabel(), ts.getFormattedRange());
        }
        System.out.print("Enter Slot ID (e.g. SLOT-1, SLOT-2): ");
        String slotId = scanner.nextLine().trim().toUpperCase();

        System.out.print("Enter Purpose / Academic Course (e.g. CSE2006 Lab Practice): ");
        String purpose = scanner.nextLine().trim();

        System.out.println(YELLOW + "\nProcessing transactional reservation with thread-safe lock..." + RESET);
        ReservationResult result = reservationService.makeReservation(
                authService.getCurrentUser(), resId, date, slotId, purpose
        );

        if (result.isSuccess()) {
            System.out.println(GREEN + BOLD + ">>> RESERVATION CONFIRMED SUCCESSFULLY! <<<" + RESET);
            System.out.println(result.getReservation());
            System.out.println("Execution Latency: " + result.getExecutionDurationMs() + " ms");
        } else {
            System.out.println(RED + BOLD + ">>> RESERVATION FAILED <<<" + RESET);
            System.out.println("Reason: " + result.getMessage());
        }
    }

    private void viewAndCancelMyBookings() {
        if (!authService.isLoggedIn()) {
            System.out.println(RED + "Please log in first!" + RESET);
            return;
        }

        List<Reservation> list = reservationService.getUserReservations(authService.getCurrentUser().getUserId());
        if (list.isEmpty()) {
            System.out.println(YELLOW + "No reservations found for your account." + RESET);
            return;
        }

        System.out.println(BOLD + "\n>>> YOUR RESERVATIONS:" + RESET);
        for (Reservation r : list) {
            System.out.println(r);
        }

        System.out.print("\nEnter Reservation ID to Cancel (or press ENTER to skip): ");
        String cancelId = scanner.nextLine().trim();
        if (!cancelId.isEmpty()) {
            boolean ok = reservationService.cancelReservation(authService.getCurrentUser(), cancelId);
            if (ok) {
                System.out.println(GREEN + "Reservation " + cancelId + " cancelled successfully. Slot is now free." + RESET);
            } else {
                System.out.println(RED + "Could not cancel reservation. Check ID and permissions." + RESET);
            }
        }
    }

    private void showAnalyticsDashboard() {
        System.out.println(BOLD + "\n================ INSTITUTIONAL UTILIZATION DASHBOARD ================" + RESET);
        Map<String, Object> metrics = analyticsService.getSystemAnalytics();

        System.out.println("Total Registered Facilities: " + metrics.get("totalResources"));
        System.out.println("Total Lifetime Reservations: " + metrics.get("totalBookings"));
        System.out.println("Active Confirmed Bookings  : " + GREEN + metrics.get("activeBookings") + RESET);
        System.out.println("Cancelled Bookings         : " + metrics.get("cancelledBookings"));
        System.out.println("Concurrency Locks Acquired : " + metrics.get("lockAcquisitions"));
        System.out.println("Race Collisions Prevented  : " + GREEN + metrics.get("collisionsPrevented") + RESET);

        System.out.println("\nRole-Based Bookings Distribution:");
        @SuppressWarnings("unchecked")
        Map<UserRole, Long> roleMap = (Map<UserRole, Long>) metrics.get("roleDistribution");
        if (roleMap != null) {
            for (Map.Entry<UserRole, Long> e : roleMap.entrySet()) {
                System.out.printf("  • %-15s: %d bookings%n", e.getKey().getDisplayName(), e.getValue());
            }
        }

        System.out.println("\nTop Reserved Campus Resources:");
        @SuppressWarnings("unchecked")
        List<Map.Entry<String, Long>> top = (List<Map.Entry<String, Long>>) metrics.get("topResources");
        if (top != null && !top.isEmpty()) {
            for (Map.Entry<String, Long> entry : top) {
                System.out.printf("  • %-35s: %d bookings%n", entry.getKey(), entry.getValue());
            }
        } else {
            System.out.println("  (No booking history yet)");
        }
        System.out.println("=====================================================================");
    }

    private void adminMaintenanceControl() {
        if (!authService.isAdmin()) {
            System.out.println(RED + "Access Denied! Requires LAB_ADMIN clearance." + RESET);
            return;
        }

        System.out.print("\nEnter Resource ID to toggle (e.g. LAB-SCSE-101): ");
        String resId = scanner.nextLine().trim();
        Resource res = resourceService.getResourceById(resId);
        if (res == null) {
            System.out.println(RED + "Resource not found." + RESET);
            return;
        }

        System.out.println("Current Status: " + res.getStatus());
        System.out.println("Select New Status: 1. AVAILABLE  2. MAINTENANCE  3. DECOMMISSIONED");
        System.out.print("Choice: ");
        String ch = scanner.nextLine().trim();

        ResourceStatus newStatus = ResourceStatus.AVAILABLE;
        if ("2".equals(ch)) newStatus = ResourceStatus.MAINTENANCE;
        else if ("3".equals(ch)) newStatus = ResourceStatus.DECOMMISSIONED;

        resourceService.updateResourceStatus(resId, newStatus);
        System.out.println(GREEN + "Resource " + resId + " updated to " + newStatus + RESET);
    }

    private void switchUserFlow() {
        System.out.println("\nSwitch Active Account:");
        System.out.println("  1. Student: Rohan Chetty (23BCE10045) [SCSE]");
        System.out.println("  2. Student: Ananya Sharma (23BCE10210) [SCOPE]");
        System.out.println("  3. Faculty: Dr. Rajesh K. (EMP-5082) [SCSE]");
        System.out.println("  4. Lab Admin: Vikram Singh (ADM-004) [AB-1]");
        System.out.print("Select User [1-4]: ");
        String ch = scanner.nextLine().trim();

        switch (ch) {
            case "1":
                authService.login("USR-STD-01", "vit@2024");
                break;
            case "2":
                authService.login("USR-STD-02", "vit@2024");
                break;
            case "3":
                authService.login("USR-FAC-01", "vit@fac");
                break;
            case "4":
                authService.login("USR-ADM-01", "vit@admin");
                break;
            default:
                System.out.println(RED + "Unknown selection." + RESET);
                return;
        }
        System.out.println(GREEN + "Logged in as: " + authService.getCurrentUser().getName() + RESET);
    }

    private void runLiveConcurrencyTest() {
        System.out.println(YELLOW + "\nLaunching real-time 10-thread parallel race-condition stress test..." + RESET);
        new ReservationTestSuite().runAllTests();
    }

    private LocalDate getTargetDateFromUser() {
        System.out.print("Enter Date (YYYY-MM-DD) or press ENTER for tomorrow: ");
        String dateStr = scanner.nextLine().trim();
        if (dateStr.isEmpty()) {
            return LocalDate.now().plusDays(1);
        }
        try {
            return LocalDate.parse(dateStr, DATE_FMT);
        } catch (Exception e) {
            System.out.println(YELLOW + "Invalid format, defaulting to tomorrow." + RESET);
            return LocalDate.now().plusDays(1);
        }
    }
}
