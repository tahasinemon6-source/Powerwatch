package ui;

import dao.TechnicianDAO;
import model.Technician;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TechnicianManagementFrame extends JPanel {
    private JTable tblTechnicians;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JButton btnAdd, btnUpdate, btnDelete;
    private TechnicianDAO technicianDAO;
    private User currentUser;

    public TechnicianManagementFrame(User user) {
        this.currentUser = user;
        this.technicianDAO = new TechnicianDAO();
        initUI();
        refreshData();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header and Search panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel lblTitle = new JLabel("Technician Dispatch & Teams");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(30, 41, 59));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchPanel.setBackground(Color.WHITE);

        txtSearch = new JTextField(15);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSearch.putClientProperty("JTextField.placeholderText", "Search by name/team/status...");

        JButton btnSearch = new JButton("Search");
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
            txtSearch.setText("");
            refreshData();
        });

        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnClear);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(searchPanel, BorderLayout.EAST);

        // Table
        String[] columns = {"ID", "Name", "Phone Number", "Team Name", "Availability Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tblTechnicians = new JTable(tableModel);
        tblTechnicians.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblTechnicians.setRowHeight(25);
        tblTechnicians.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblTechnicians.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(tblTechnicians);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        // Action Buttons (Add, Update, Delete)
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        actionPanel.setBackground(Color.WHITE);

        btnAdd = new JButton("Add Technician");
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAdd.setBackground(new Color(37, 99, 235));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.addActionListener(e -> openAddDialog());

        btnUpdate = new JButton("Update Status/Details");
        btnUpdate.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnUpdate.setBackground(new Color(245, 158, 11)); // Amber 500
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.setFocusPainted(false);
        btnUpdate.addActionListener(e -> openUpdateDialog());

        btnDelete = new JButton("Delete Technician");
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDelete.setBackground(new Color(239, 68, 68)); // Red 500
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFocusPainted(false);
        btnDelete.addActionListener(e -> handleDelete());

        actionPanel.add(btnAdd);
        actionPanel.add(btnUpdate);
        actionPanel.add(btnDelete);

        // Role-based access control (Admin gets everything, operator can add/update, admin can delete)
        if (!currentUser.isAdmin()) {
            btnDelete.setEnabled(false);
            btnDelete.setToolTipText("Only Administrators can delete technicians.");
        }

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        List<Technician> list = technicianDAO.getAllTechnicians();
        for (Technician t : list) {
            tableModel.addRow(new Object[]{
                t.getTechnicianId(),
                t.getTechnicianName(),
                t.getPhone(),
                t.getTeamName(),
                t.getAvailabilityStatus()
            });
        }
    }

    private void performSearch() {
        String query = txtSearch.getText().trim();
        if (query.isEmpty()) {
            refreshData();
            return;
        }
        tableModel.setRowCount(0);
        List<Technician> list = technicianDAO.searchTechnicians(query);
        for (Technician t : list) {
            tableModel.addRow(new Object[]{
                t.getTechnicianId(),
                t.getTechnicianName(),
                t.getPhone(),
                t.getTeamName(),
                t.getAvailabilityStatus()
            });
        }
    }

    private void openAddDialog() {
        JTextField nameField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField teamField = new JTextField();
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"AVAILABLE", "BUSY", "OFFLINE"});

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.add(new JLabel("Full Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Phone number:"));
        panel.add(phoneField);
        panel.add(new JLabel("Team Name:"));
        panel.add(teamField);
        panel.add(new JLabel("Initial Status:"));
        panel.add(statusCombo);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Technician", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String team = teamField.getText().trim();
            String status = (String) statusCombo.getSelectedItem();

            if (name.isEmpty() || phone.isEmpty() || team.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Technician tech = new Technician(name, phone, team, status);
            if (technicianDAO.addTechnician(tech)) {
                JOptionPane.showMessageDialog(this, "Technician registered successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to register technician.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openUpdateDialog() {
        int selectedRow = tblTechnicians.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a technician from the table first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int techId = (int) tableModel.getValueAt(selectedRow, 0);
        Technician tech = technicianDAO.getTechnicianById(techId);
        if (tech == null) return;

        JTextField nameField = new JTextField(tech.getTechnicianName());
        JTextField phoneField = new JTextField(tech.getPhone());
        JTextField teamField = new JTextField(tech.getTeamName());
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"AVAILABLE", "BUSY", "OFFLINE"});
        statusCombo.setSelectedItem(tech.getAvailabilityStatus());

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.add(new JLabel("Full Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Phone number:"));
        panel.add(phoneField);
        panel.add(new JLabel("Team Name:"));
        panel.add(teamField);
        panel.add(new JLabel("Availability Status:"));
        panel.add(statusCombo);

        int result = JOptionPane.showConfirmDialog(this, panel, "Update Technician Details", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String team = teamField.getText().trim();
            String status = (String) statusCombo.getSelectedItem();

            if (name.isEmpty() || phone.isEmpty() || team.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            tech.setTechnicianName(name);
            tech.setPhone(phone);
            tech.setTeamName(team);
            tech.setAvailabilityStatus(status);

            if (technicianDAO.updateTechnician(tech)) {
                JOptionPane.showMessageDialog(this, "Technician details updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update details.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDelete() {
        int selectedRow = tblTechnicians.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a technician from the table first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int techId = (int) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);

        int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove technician: " + name + "?", 
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (response == JOptionPane.YES_OPTION) {
            if (technicianDAO.deleteTechnician(techId)) {
                JOptionPane.showMessageDialog(this, "Technician deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete technician. They may be linked to outage reports.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
