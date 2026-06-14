package ui;

import dao.NotificationDAO;
import model.Notification;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class NotificationFrame extends JPanel {
    private JTable tblNotifications;
    private DefaultTableModel tableModel;
    private NotificationDAO notificationDAO;

    public NotificationFrame() {
        notificationDAO = new NotificationDAO();
        initUI();
        refreshData();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel lblTitle = new JLabel("System Notifications History");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(30, 41, 59));

        JButton btnRefresh = new JButton("Refresh Logs");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.setBackground(new Color(37, 99, 235));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> refreshData());

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnRefresh, BorderLayout.EAST);

        // Table
        String[] columnNames = {"Notification ID", "Outage ID", "Event Message", "Timestamp"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only
            }
        };

        tblNotifications = new JTable(tableModel);
        tblNotifications.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblNotifications.setRowHeight(25);
        tblNotifications.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblNotifications.getTableHeader().setReorderingAllowed(false);
        
        // Column sizing
        tblNotifications.getColumnModel().getColumn(0).setPreferredWidth(100);
        tblNotifications.getColumnModel().getColumn(1).setPreferredWidth(80);
        tblNotifications.getColumnModel().getColumn(2).setPreferredWidth(500);
        tblNotifications.getColumnModel().getColumn(3).setPreferredWidth(180);

        JScrollPane scrollPane = new JScrollPane(tblNotifications);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        List<Notification> list = notificationDAO.getAllNotifications();
        for (Notification notif : list) {
            tableModel.addRow(new Object[]{
                notif.getNotificationId(),
                notif.getOutageId() != null && notif.getOutageId() > 0 ? "#" + notif.getOutageId() : "N/A",
                notif.getMessage(),
                notif.getCreatedTime()
            });
        }
    }
}
