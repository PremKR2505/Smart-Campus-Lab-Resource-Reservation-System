package com.campus.reserve;

import com.campus.reserve.config.CampusConfig;
import com.campus.reserve.service.*;
import com.campus.reserve.storage.DataStorage;
import com.campus.reserve.storage.FileDataStorage;
import com.campus.reserve.test.ReservationTestSuite;
import com.campus.reserve.ui.CampusReserveGUI;
import com.campus.reserve.ui.ConsoleUI;

import javax.swing.*;

/**
 * Main Entry Point for Smart Campus Lab & Resource Reservation System.
 * Supports flexible launch modes: CLI interactive terminal, GUI Swing desktop,
 * and automated verification test suite.
 */
public class Main {

    public static void main(String[] args) {
        String mode = "--cli"; // Default mode

        if (args.length > 0) {
            mode = args[0].trim().toLowerCase();
        }

        if (mode.equals("--help") || mode.equals("-h")) {
            printUsage();
            return;
        }

        if (mode.equals("--test")) {
            System.out.println("Running automated test suite in verification mode...\n");
            boolean passed = new ReservationTestSuite().runAllTests();
            System.exit(passed ? 0 : 1);
            return;
        }

        // Initialize core dependencies
        DataStorage storage = new FileDataStorage();
        AuthService authService = new AuthService(storage);
        ResourceService resourceService = new ResourceService(storage);
        ReservationService reservationService = new ReservationService(storage);
        AnalyticsService analyticsService = new AnalyticsService(storage);

        if (mode.equals("--gui")) {
            System.out.println("Launching " + CampusConfig.SYSTEM_NAME + " Desktop GUI...");
            SwingUtilities.invokeLater(() -> {
                CampusReserveGUI gui = new CampusReserveGUI(
                        storage, authService, resourceService, reservationService, analyticsService
                );
                gui.setVisible(true);
            });
        } else {
            // Interactive CLI
            ConsoleUI cli = new ConsoleUI(
                    storage, authService, resourceService, reservationService, analyticsService
            );
            cli.start();
        }
    }

    private static void printUsage() {
        System.out.println("===============================================================");
        System.out.println("  " + CampusConfig.SYSTEM_NAME);
        System.out.println("  " + CampusConfig.INSTITUTION_NAME);
        System.out.println("===============================================================");
        System.out.println("Usage:");
        System.out.println("  java -cp bin com.campus.reserve.Main [OPTION]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --cli     (Default) Launch rich interactive terminal CLI");
        System.out.println("  --gui     Launch modern desktop graphical user interface");
        System.out.println("  --test    Execute complete automated verification test suite");
        System.out.println("  --help    Show this command guide");
        System.out.println("===============================================================");
    }
}
