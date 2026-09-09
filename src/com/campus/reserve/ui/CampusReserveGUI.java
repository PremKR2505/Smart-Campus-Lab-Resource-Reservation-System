package com.campus.reserve.ui;

import com.campus.reserve.concurrency.ReservationResult;
import com.campus.reserve.concurrency.SlotLockManager;
import com.campus.reserve.config.CampusConfig;
import com.campus.reserve.model.*;
import com.campus.reserve.service.*;
import com.campus.reserve.storage.DataStorage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * CampusReserveGUI delivers a modern, interactive desktop interface
 * built with Java Swing for faculty and students to browse, inspect,
 * and reserve campus lab slots in real-time.
 */
public class CampusReserveGUI extends JFrame {

    private final DataStorage storage;
    private final AuthService authService;
    private final ResourceService resourceService;
    private final ReservationService reservationService;
    private final AnalyticsService analyticsService;

    // UI Components
    private JComboBox<String> userSelector;
    private JComboBox<String> campusSelector;
    private JTable resourceTable;
    private DefaultTableModel resourceTableModel;
    private JTable reservationTable;
    private DefaultTableModel reservationTableModel;

    private JLabel lblSelectedResource;
    private JLabel lblResourceSpecs;
    private JPanel slotButtonsPanel;
    private JLabel lblStatusMessage;
    private JLabel lblTelemetry;

    private Resource currentSelectedResource;
    private LocalDate selectedDate = LocalDate.now().plusDays(1);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public CampusReserveGUI(DataStorage storage, AuthService authService,
                            ResourceService resourceService, ReservationService reservationService,
                            AnalyticsService analyticsService) {
        this.storage = storage;
        this.authService = authService;
        this.resourceService = resourceService;
        this.reservationService = reservationService;
        this.analyticsService = analyticsService;

        initUI();
    }

    private void initUI() {
        setTitle(CampusConfig.SYSTEM_NAME + " - " + CampusConfig.INSTITUTION_NAME);
        setSize(1100, 720);
        setMinimumSize(new Dimension(950, 640));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Set modern look and feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainSplitPane(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);

        // Initial Data Load
        refreshResourceTable();
        refreshReservationTable();
        updateTelemetry();
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(15, 10));
        header.setBackground(new Color(24, 43, 73));
        header.setBorder(new EmptyBorder(12, 18, 12, 18));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 2, 2));
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel(CampusConfig.SYSTEM_NAME);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 19));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel(CampusConfig.INSTITUTION_NAME + " | " + CampusConfig.SYSTEM_TAGLINE);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(180, 205, 235));

        titlePanel.add(lblTitle);
        titlePanel.add(lblSub);

        // Right controls: Campus Preset & Active User Switcher
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);

        JLabel lblCamp = new JLabel("Campus:");
        lblCamp.setForeground(Color.WHITE);
        campusSelector = new JComboBox<>(new String[]{
                "VIT Bhopal Campus", "VIT Vellore Campus", "VIT Chennai Campus", "VIT-AP Campus"
        });
        campusSelector.addActionListener(e -> {
            CampusConfig.setActiveCampus((String) campusSelector.getSelectedItem());
            lblStatusMessage.setText("Campus switched to: " + CampusConfig.getActiveCampus());
        });

        JLabel lblUser = new JLabel("Logged In:");
        lblUser.setForeground(Color.WHITE);
        userSelector = new JComboBox<>(new String[]{
                "Rohan Chetty (Student: 23BCE10045)",
                "Ananya Sharma (Student: 23BCE10210)",
                "Dr. Rajesh K. (Faculty: EMP-5082)",
                "Vikram Singh (Lab Admin: ADM-004)"
        });

        // Set initial user
        authService.login("USR-STD-01", "vit@2024");
        userSelector.addActionListener(e -> handleUserSwitch());

        controls.add(lblCamp);
        controls.add(campusSelector);
        controls.add(lblUser);
        controls.add(userSelector);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(controls, BorderLayout.EAST);
        return header;
    }

    private void handleUserSwitch() {
        int idx = userSelector.getSelectedIndex();
        switch (idx) {
            case 0: authService.login("USR-STD-01", "vit@2024"); break;
            case 1: authService.login("USR-STD-02", "vit@2024"); break;
            case 2: authService.login("USR-FAC-01", "vit@fac"); break;
            case 3: authService.login("USR-ADM-01", "vit@admin"); break;
        }
        refreshReservationTable();
        lblStatusMessage.setText("Active User: " + authService.getCurrentUser().getName()
                + " (Quota: " + authService.getCurrentUser().getMaxAllowedBookings() + ")");
    }

    private JSplitPane createMainSplitPane() {
        // Left: Resource Catalog Table
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Campus Facilities Catalog", TitledBorder.LEFT, TitledBorder.TOP));

        resourceTableModel = new DefaultTableModel(new String[]{"ID", "Resource Name", "Block / Location", "Cap", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        resourceTable = new JTable(resourceTableModel);
        resourceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resourceTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && resourceTable.getSelectedRow() != -1) {
                String id = (String) resourceTableModel.getValueAt(resourceTable.getSelectedRow(), 0);
                onResourceSelected(id);
            }
        });

        leftPanel.add(new JScrollPane(resourceTable), BorderLayout.CENTER);

        // Right: Resource Booking & Details Center
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Reservation Control & Slot Schedule", TitledBorder.LEFT, TitledBorder.TOP));

        // Details Subpanel
        JPanel detailsPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        detailsPanel.setBackground(new Color(245, 248, 252));
        detailsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        lblSelectedResource = new JLabel("Select a facility from the catalog on the left");
        lblSelectedResource.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSelectedResource.setForeground(new Color(15, 35, 65));

        lblResourceSpecs = new JLabel("Specifications will appear here.");
        lblResourceSpecs.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblResourceSpecs.setForeground(Color.DARK_GRAY);

        detailsPanel.add(lblSelectedResource);
        detailsPanel.add(lblResourceSpecs);

        rightPanel.add(detailsPanel, BorderLayout.NORTH);

        // Slot Grid Panel
        JPanel gridWrapper = new JPanel(new BorderLayout(5, 5));
        gridWrapper.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel lblGridTitle = new JLabel("Select Operational Time Slot (Date: " + selectedDate.format(DATE_FMT) + "):");
        lblGridTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gridWrapper.add(lblGridTitle, BorderLayout.NORTH);

        slotButtonsPanel = new JPanel(new GridLayout(5, 1, 8, 8));
        gridWrapper.add(slotButtonsPanel, BorderLayout.CENTER);

        rightPanel.add(gridWrapper, BorderLayout.CENTER);

        // Active Reservations Panel at bottom of right
        JPanel myBookingsPanel = new JPanel(new BorderLayout(5, 5));
        myBookingsPanel.setBorder(BorderFactory.createTitledBorder("Active Reservations"));

        reservationTableModel = new DefaultTableModel(new String[]{"Res ID", "Resource", "Date", "Slot", "User", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        reservationTable = new JTable(reservationTableModel);
        reservationTable.setPreferredScrollableViewportSize(new Dimension(450, 120));
        myBookingsPanel.add(new JScrollPane(reservationTable), BorderLayout.CENTER);

        JButton btnCancel = new JButton("Cancel Selected Reservation");
        btnCancel.addActionListener(e -> handleCancelReservation());
        myBookingsPanel.add(btnCancel, BorderLayout.SOUTH);

        rightPanel.add(myBookingsPanel, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        split.setDividerLocation(480);
        return split;
    }

    private void onResourceSelected(String resourceId) {
        currentSelectedResource = resourceService.getResourceById(resourceId);
        if (currentSelectedResource == null) return;

        lblSelectedResource.setText(currentSelectedResource.getName() + " (" + currentSelectedResource.getBlockName() + ")");
        lblResourceSpecs.setText("<html><b>Specs:</b> " + currentSelectedResource.getTechnicalSpecifications() + "</html>");

        renderSlotButtons();
    }

    private void renderSlotButtons() {
        slotButtonsPanel.removeAll();
        if (currentSelectedResource == null) {
            slotButtonsPanel.revalidate();
            slotButtonsPanel.repaint();
            return;
        }

        Map<TimeSlot, Boolean> availability = resourceService.getSlotAvailability(
                currentSelectedResource.getResourceId(), selectedDate);

        for (Map.Entry<TimeSlot, Boolean> entry : availability.entrySet()) {
            TimeSlot slot = entry.getKey();
            boolean isAvailable = entry.getValue();

            String labelText = String.format("%s | %s (%s) - %s",
                    slot.getSlotId(), slot.getLabel(), slot.getFormattedRange(),
                    isAvailable ? "AVAILABLE [Click to Book]" : "RESERVED / OCCUPIED");

            JButton btn = new JButton(labelText);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            if (isAvailable) {
                btn.setBackground(new Color(220, 245, 225));
                btn.setForeground(new Color(20, 95, 40));
                btn.addActionListener(e -> bookSlotPrompt(slot));
            } else {
                btn.setBackground(new Color(250, 225, 225));
                btn.setForeground(new Color(140, 30, 30));
                btn.setEnabled(false);
            }
            slotButtonsPanel.add(btn);
        }
        slotButtonsPanel.revalidate();
        slotButtonsPanel.repaint();
    }

    private void bookSlotPrompt(TimeSlot slot) {
        User user = authService.getCurrentUser();
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Please select an active user to book!", "Authentication", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String purpose = JOptionPane.showInputDialog(this,
                "Booking: " + currentSelectedResource.getName() + "\nSlot: " + slot.getFormattedRange()
                        + "\n\nEnter Academic Purpose / Subject:",
                "Confirm Reservation Request", JOptionPane.QUESTION_MESSAGE);

        if (purpose != null) {
            ReservationResult result = reservationService.makeReservation(
                    user, currentSelectedResource.getResourceId(), selectedDate, slot.getSlotId(), purpose);

            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this,
                        "Reservation Confirmed!\nReservation ID: " + result.getReservation().getReservationId()
                                + "\nLatency: " + result.getExecutionDurationMs() + "ms",
                        "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);
                lblStatusMessage.setText("Booking confirmed: " + result.getReservation().getReservationId());
            } else {
                JOptionPane.showMessageDialog(this,
                        "Reservation Failed!\n" + result.getMessage(),
                        "Booking Error", JOptionPane.ERROR_MESSAGE);
                lblStatusMessage.setText("Error: " + result.getMessage());
            }
            renderSlotButtons();
            refreshReservationTable();
            updateTelemetry();
        }
    }

    private void handleCancelReservation() {
        int row = reservationTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a reservation from the table to cancel.", "Cancel", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String resId = (String) reservationTableModel.getValueAt(row, 0);
        boolean ok = reservationService.cancelReservation(authService.getCurrentUser(), resId);
        if (ok) {
            lblStatusMessage.setText("Cancelled reservation " + resId);
            refreshReservationTable();
            renderSlotButtons();
            updateTelemetry();
        } else {
            JOptionPane.showMessageDialog(this, "Could not cancel reservation. Only owners or admins may cancel.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshResourceTable() {
        resourceTableModel.setRowCount(0);
        List<Resource> list = resourceService.getAllResources();
        for (Resource r : list) {
            resourceTableModel.addRow(new Object[]{
                    r.getResourceId(), r.getName(), r.getBlockName(), r.getCapacity(), r.getStatus().name()
            });
        }
        if (resourceTable.getRowCount() > 0) {
            resourceTable.setRowSelectionInterval(0, 0);
        }
    }

    private void refreshReservationTable() {
        reservationTableModel.setRowCount(0);
        List<Reservation> list = reservationService.getAllReservations();
        for (Reservation r : list) {
            reservationTableModel.addRow(new Object[]{
                    r.getReservationId(), r.getResourceName(), r.getReservationDate(),
                    r.getTimeSlotId(), r.getUserName(), r.getStatus().name()
            });
        }
    }

    private void updateTelemetry() {
        SlotLockManager lockMgr = SlotLockManager.getInstance();
        lblTelemetry.setText(String.format("Telemetry | Locks Acquired: %d | Contention Conflicts Prevented: %d",
                lockMgr.getTotalLockAcquisitions(), lockMgr.getTotalCollisionsPrevented()));
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout(10, 5));
        footer.setBorder(new EmptyBorder(6, 12, 6, 12));
        footer.setBackground(new Color(235, 240, 248));

        lblStatusMessage = new JLabel("System Ready. " + CampusConfig.SYSTEM_VERSION);
        lblStatusMessage.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        lblTelemetry = new JLabel("Telemetry Initializing...");
        lblTelemetry.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTelemetry.setForeground(new Color(30, 80, 150));

        footer.add(lblStatusMessage, BorderLayout.WEST);
        footer.add(lblTelemetry, BorderLayout.EAST);
        return footer;
    }
}
