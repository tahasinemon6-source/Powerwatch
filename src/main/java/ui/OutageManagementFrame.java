package ui;

import dao.InfrastructureDAO;
import dao.OutageDAO;
import dao.TechnicianDAO;
import model.Outage;
import model.Technician;
import model.User;
import service.NotificationService;
import service.PredictionService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

public class OutageManagementFrame extends JPanel {
    private JTable tblOutages;
    private DefaultTableModel tableModel;
    private JTextField txtSearchArea, txtSearchDate;
    private JComboBox<String> cbFilterStatus;
    private JButton btnCreate, btnUpdate, btnDelete;
    
    private final OutageDAO outageDAO;
    private final TechnicianDAO technicianDAO;
    private final InfrastructureDAO infrastructureDAO;
    private final PredictionService predictionService;
    private final NotificationService notificationService;
    private final User currentUser;

    public OutageManagementFrame(User user) {
        this.currentUser = user;
        this.outageDAO = new OutageDAO();
        this.technicianDAO = new TechnicianDAO();
        this.infrastructureDAO = new InfrastructureDAO();
        this.predictionService = new PredictionService(outageDAO);
        this.notificationService = new NotificationService();
        
        initUI();
        refreshData();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header Panel with Filters
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel lblTitle = new JLabel("Emergency Outage & Fault Incidents");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(30, 41, 59));
        headerPanel.add(lblTitle, BorderLayout.WEST);

        // Filters Panel
        JPanel filtersPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filtersPanel.setBackground(Color.WHITE);

        JLabel lblArea = new JLabel("Area:");
        lblArea.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtSearchArea = new JTextField(10);

        JLabel lblStatus = new JLabel("Status:");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cbFilterStatus = new JComboBox<>(new String[]{"ALL", "REPORTED", "TECHNICIAN_ASSIGNED", "REPAIR_STARTED", "POWER_RESTORED"});

        JLabel lblDate = new JLabel("Date (YYYY-MM-DD):");
        lblDate.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtSearchDate = new JTextField(8);

        JButton btnSearch = new JButton("Filter");
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSearch.setBackground(new Color(71, 85, 105));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFocusPainted(false);
        btnSearch.addActionListener(e -> performSearch());

        JButton btnClear = new JButton("Clear");
        btnClear.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnClear.setBackground(new Color(226, 232, 240));
        btnClear.setForeground(new Color(71, 85, 105));
        btnClear.setFocusPainted(false);
        btnClear.addActionListener(e -> {
            txtSearchArea.setText("");
            txtSearchDate.setText("");
            cbFilterStatus.setSelectedIndex(0);
            refreshData();
        });

        filtersPanel.add(lblArea);
        filtersPanel.add(txtSearchArea);
        filtersPanel.add(lblStatus);
        filtersPanel.add(cbFilterStatus);
        filtersPanel.add(lblDate);
        filtersPanel.add(txtSearchDate);
        filtersPanel.add(btnSearch);
        filtersPanel.add(btnClear);
        headerPanel.add(filtersPanel, BorderLayout.EAST);

        // Outages Table
        String[] columns = {"ID", "Area Name", "Type", "Priority", "Cause", "Reported Time", "Est. Restore Time", "Actual Restore", "Status", "Technician"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tblOutages = new JTable(tableModel);
        tblOutages.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblOutages.setRowHeight(25);
        tblOutages.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblOutages.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(tblOutages);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        // Action Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        actionPanel.setBackground(Color.WHITE);

        btnCreate = new JButton("Create Outage Report");
        btnCreate.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCreate.setBackground(new Color(37, 99, 235));
        btnCreate.setForeground(Color.WHITE);
        btnCreate.setFocusPainted(false);
        btnCreate.addActionListener(e -> openCreateDialog());

        btnUpdate = new JButton("Update Status / Details");
        btnUpdate.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnUpdate.setBackground(new Color(245, 158, 11));
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.setFocusPainted(false);
        btnUpdate.addActionListener(e -> openUpdateDialog());

        btnDelete = new JButton("Delete Incident");
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDelete.setBackground(new Color(239, 68, 68));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFocusPainted(false);
        btnDelete.addActionListener(e -> handleDelete());

        actionPanel.add(btnCreate);
        actionPanel.add(btnUpdate);
        actionPanel.add(btnDelete);

        // Role-based access control
        if (!currentUser.isAdmin()) {
            btnDelete.setEnabled(false);
            btnDelete.setToolTipText("Only Administrators can delete outage reports.");
        }

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        List<Outage> list = outageDAO.getAllOutages();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (Outage o : list) {
            String techName = "None";
            if (o.getAssignedTechnicianId() != null) {
                Technician tech = technicianDAO.getTechnicianById(o.getAssignedTechnicianId());
                if (tech != null) {
                    techName = tech.getTechnicianName() + " (" + tech.getTeamName() + ")";
                }
            }
            
            tableModel.addRow(new Object[]{
                o.getOutageId(),
                o.getAreaName(),
                o.getOutageType(),
                o.getPriority(),
                o.getCause(),
                o.getReportTime() != null ? sdf.format(o.getReportTime()) : "",
                o.getEstimatedRestoreTime() != null ? sdf.format(o.getEstimatedRestoreTime()) : "N/A",
                o.getActualRestoreTime() != null ? sdf.format(o.getActualRestoreTime()) : "N/A",
                o.getStatus(),
                techName
            });
        }
    }

    private void performSearch() {
        String area = txtSearchArea.getText().trim();
        String status = (String) cbFilterStatus.getSelectedItem();
        String date = txtSearchDate.getText().trim();

        if (!date.isEmpty() && !date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this, "Date must follow YYYY-MM-DD format.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        tableModel.setRowCount(0);
        List<Outage> list = outageDAO.searchOutages(area, status, date);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (Outage o : list) {
            String techName = "None";
            if (o.getAssignedTechnicianId() != null) {
                Technician tech = technicianDAO.getTechnicianById(o.getAssignedTechnicianId());
                if (tech != null) {
                    techName = tech.getTechnicianName() + " (" + tech.getTeamName() + ")";
                }
            }
            
            tableModel.addRow(new Object[]{
                o.getOutageId(),
                o.getAreaName(),
                o.getOutageType(),
                o.getPriority(),
                o.getCause(),
                o.getReportTime() != null ? sdf.format(o.getReportTime()) : "",
                o.getEstimatedRestoreTime() != null ? sdf.format(o.getEstimatedRestoreTime()) : "N/A",
                o.getActualRestoreTime() != null ? sdf.format(o.getActualRestoreTime()) : "N/A",
                o.getStatus(),
                techName
            });
        }
    }

    private void openCreateDialog() {
        JTextField areaField = new JTextField();
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Emergency", "Unscheduled", "Scheduled", "Maintenance"});
        JComboBox<String> causeCombo = new JComboBox<>(new String[]{"Transformer Failure", "Weather", "Grid Overload", "Equipment Failure", "Maintenance", "Accident"});
        JLabel lblPrediction = new JLabel("Predicted Restoration Time: -- Hours");
        lblPrediction.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPrediction.setForeground(new Color(37, 99, 235));

        JComboBox<String> priorityCombo = new JComboBox<>(new String[]{"LOW", "MEDIUM", "HIGH", "CRITICAL"});

        // Dynamic Prediction Update
        causeCombo.addActionListener(e -> {
            String selectedCause = (String) causeCombo.getSelectedItem();
            double predictedHours = predictionService.predictRestorationTimeHours(selectedCause);
            lblPrediction.setText("Predicted Restoration Time: " + predictedHours + " Hours");
        });

        // Set initial prediction
        double predictedHours = predictionService.predictRestorationTimeHours((String) causeCombo.getSelectedItem());
        lblPrediction.setText("Predicted Restoration Time: " + predictedHours + " Hours");

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.add(new JLabel("Area Name:"));
        panel.add(areaField);
        panel.add(new JLabel("Outage Type:"));
        panel.add(typeCombo);
        panel.add(new JLabel("Outage Cause:"));
        panel.add(causeCombo);
        panel.add(new JLabel("Estimation:"));
        panel.add(lblPrediction);
        panel.add(new JLabel("Default Priority:"));
        panel.add(priorityCombo);

        int result = JOptionPane.showConfirmDialog(this, panel, "Log New Outage Report", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String areaName = areaField.getText().trim();
            String outageType = (String) typeCombo.getSelectedItem();
            String cause = (String) causeCombo.getSelectedItem();
            String priority = (String) priorityCombo.getSelectedItem();

            if (areaName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Area Name is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 1. Duplicate Outage Detection
            if (outageDAO.hasActiveOutageInArea(areaName)) {
                JOptionPane.showMessageDialog(this, "Outage already exists for this area (" + areaName + ").\nDo not insert duplicate record.",
                        "Duplicate Outage Detected", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 2. Critical Infrastructure Detection
            boolean isCritical = infrastructureDAO.isCriticalArea(areaName);
            if (isCritical) {
                priority = "CRITICAL";
                JOptionPane.showMessageDialog(this, "CRITICAL ALERT\nHospital or Public Safety Area Affected in " + areaName + "!\nPriority automatically updated to CRITICAL.",
                        "Critical Infrastructure Detection", JOptionPane.WARNING_MESSAGE);
            }

            // Calculate estimated restoration time based on predicted hours
            double prediction = predictionService.predictRestorationTimeHours(cause);
            long millisToAdd = (long) (prediction * 3600 * 1000);
            Timestamp reportTime = new Timestamp(System.currentTimeMillis());
            Timestamp estRestoreTime = new Timestamp(reportTime.getTime() + millisToAdd);

            Outage outage = new Outage(areaName, outageType, priority, cause, estRestoreTime, "REPORTED");
            outage.setReportTime(reportTime);

            int outageId = outageDAO.createOutage(outage, currentUser.getUsername());
            if (outageId > 0) {
                JOptionPane.showMessageDialog(this, "Outage reported successfully (ID #" + outageId + ").", "Success", JOptionPane.INFORMATION_MESSAGE);
                
                // Create Notification
                notificationService.notifyNewOutage(outageId, areaName, cause, isCritical);
                
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create outage report.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openUpdateDialog() {
        int selectedRow = tblOutages.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an outage from the table first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int outageId = (int) tableModel.getValueAt(selectedRow, 0);
        Outage outage = outageDAO.getOutageById(outageId);
        if (outage == null) return;

        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"REPORTED", "TECHNICIAN_ASSIGNED", "REPAIR_STARTED", "POWER_RESTORED"});
        statusCombo.setSelectedItem(outage.getStatus());

        List<Technician> availableTechs = technicianDAO.getAvailableTechnicians();
        JComboBox<Object> techCombo = new JComboBox<>();
        techCombo.addItem("None (Unassigned)");
        for (Technician t : availableTechs) {
            techCombo.addItem(t);
        }

        // If tech is currently assigned, add them to combo and select them
        if (outage.getAssignedTechnicianId() != null) {
            Technician curTech = technicianDAO.getTechnicianById(outage.getAssignedTechnicianId());
            if (curTech != null) {
                boolean found = false;
                for (int i = 0; i < techCombo.getItemCount(); i++) {
                    Object item = techCombo.getItemAt(i);
                    if (item instanceof Technician && ((Technician) item).getTechnicianId() == curTech.getTechnicianId()) {
                        techCombo.setSelectedIndex(i);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    techCombo.addItem(curTech);
                    techCombo.setSelectedItem(curTech);
                }
            }
        }

        // Disable techCombo if status is REPORTED and force TECHNICIAN_ASSIGNED if tech selected
        techCombo.addActionListener(e -> {
            Object selected = techCombo.getSelectedItem();
            if (selected instanceof Technician && statusCombo.getSelectedItem().equals("REPORTED")) {
                statusCombo.setSelectedItem("TECHNICIAN_ASSIGNED");
            }
        });

        // Ensure state validations on status transitions
        statusCombo.addActionListener(e -> {
            String selStatus = (String) statusCombo.getSelectedItem();
            if ("REPORTED".equals(selStatus)) {
                techCombo.setSelectedIndex(0);
            }
        });

        JTextField causeField = new JTextField(outage.getCause());
        JComboBox<String> priorityCombo = new JComboBox<>(new String[]{"LOW", "MEDIUM", "HIGH", "CRITICAL"});
        priorityCombo.setSelectedItem(outage.getPriority());

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.add(new JLabel("Incident Status:"));
        panel.add(statusCombo);
        panel.add(new JLabel("Assign Team/Tech:"));
        panel.add(techCombo);
        panel.add(new JLabel("Cause Details:"));
        panel.add(causeField);
        panel.add(new JLabel("Priority Level:"));
        panel.add(priorityCombo);

        int result = JOptionPane.showConfirmDialog(this, panel, "Update Outage #" + outageId, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String oldStatus = outage.getStatus();
            String newStatus = (String) statusCombo.getSelectedItem();
            String newCause = causeField.getText().trim();
            String newPriority = (String) priorityCombo.getSelectedItem();

            if (newCause.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Cause details cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Object selectedTech = techCombo.getSelectedItem();
            Integer newTechId = null;
            String assignedTechName = null;
            String assignedTeamName = null;
            if (selectedTech instanceof Technician) {
                newTechId = ((Technician) selectedTech).getTechnicianId();
                assignedTechName = ((Technician) selectedTech).getTechnicianName();
                assignedTeamName = ((Technician) selectedTech).getTeamName();
            }

            // Validation: technician must be assigned if state is TECHNICIAN_ASSIGNED or REPAIR_STARTED
            if (newTechId == null && ("TECHNICIAN_ASSIGNED".equals(newStatus) || "REPAIR_STARTED".equals(newStatus))) {
                JOptionPane.showMessageDialog(this, "Please assign a technician/team to proceed in status: " + newStatus,
                        "Assignment Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            outage.setStatus(newStatus);
            outage.setAssignedTechnicianId(newTechId);
            outage.setCause(newCause);
            outage.setPriority(newPriority);

            // Set actual restoration time if status is updated to POWER_RESTORED
            if ("POWER_RESTORED".equals(newStatus)) {
                outage.setActualRestoreTime(new Timestamp(System.currentTimeMillis()));
            } else {
                outage.setActualRestoreTime(null);
            }

            if (outageDAO.updateOutage(outage, currentUser.getUsername())) {
                JOptionPane.showMessageDialog(this, "Outage details updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                
                // Notifications based on status change
                if (!oldStatus.equals(newStatus)) {
                    if ("TECHNICIAN_ASSIGNED".equals(newStatus)) {
                        notificationService.notifyTechnicianAssigned(outageId, outage.getAreaName(), assignedTechName, assignedTeamName);
                    } else if ("REPAIR_STARTED".equals(newStatus)) {
                        notificationService.notifyRepairStarted(outageId, outage.getAreaName());
                    } else if ("POWER_RESTORED".equals(newStatus)) {
                        notificationService.notifyPowerRestored(outageId, outage.getAreaName());
                    }
                }
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update outage.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDelete() {
        int selectedRow = tblOutages.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an outage from the table first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int outageId = (int) tableModel.getValueAt(selectedRow, 0);
        int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to permanently delete Outage #" + outageId + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (response == JOptionPane.YES_OPTION) {
            if (outageDAO.deleteOutage(outageId)) {
                JOptionPane.showMessageDialog(this, "Incident deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete incident.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
