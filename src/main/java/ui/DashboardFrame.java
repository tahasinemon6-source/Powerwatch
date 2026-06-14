package ui;

import dao.NotificationDAO;
import dao.OutageDAO;
import dao.TechnicianDAO;
import model.Notification;
import model.Outage;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class DashboardFrame extends JFrame {
    private final User currentUser;
    private final OutageDAO outageDAO;
    private final TechnicianDAO technicianDAO;
    private final NotificationDAO notificationDAO;

    // Sub-panels
    private OutageManagementFrame outagePanel;
    private TechnicianManagementFrame technicianPanel;
    private NotificationFrame notificationPanel;
    private HistoryFrame historyPanel;
    private StatisticsFrame statisticsPanel;

    // UI Components for Dashboard view
    private JPanel cardPanel; // right container
    private JLabel lblTotalOutages, lblActiveOutages, lblResolvedOutages, lblCriticalOutages, lblAvailableTechs;
    private JTable tblRecentOutages;
    private DefaultTableModel recentOutagesModel;
    private JList<String> listRecentNotifications;
    private DefaultListModel<String> notificationsListModel;

    public DashboardFrame(User user) {
        this.currentUser = user;
        this.outageDAO = new OutageDAO();
        this.technicianDAO = new TechnicianDAO();
        this.notificationDAO = new NotificationDAO();

        initUI();
        refreshDashboardData();
    }

    private void initUI() {
        setTitle("PowerWatch - Operations Command Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);

        // Main Container
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(new Color(240, 249, 255)); // Soft Baby Blue (Sky 50)

        // 1. Sidebar Navigation Panel
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(6, 78, 59)); // Forest Green 900
        sidebar.setPreferredSize(new Dimension(240, 800));
        sidebar.setBorder(new EmptyBorder(20, 15, 20, 15));

        // Logo
        ImageIcon logoIcon = getLogoIcon(50, 50);
        if (logoIcon != null) {
            JLabel lblLogo = new JLabel(logoIcon);
            lblLogo.setAlignmentX(Component.LEFT_ALIGNMENT);
            sidebar.add(lblLogo);
            sidebar.add(Box.createVerticalStrut(10));
        }

        // Brand Label
        JLabel lblBrand = new JLabel("POWERWATCH");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblBrand.setForeground(new Color(224, 242, 254)); // Soft Baby Blue text (Sky 100)
        lblBrand.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lblBrand);

        JLabel lblBrandSub = new JLabel("Grid Management System");
        lblBrandSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblBrandSub.setForeground(new Color(186, 230, 253)); // Soft Baby Blue subtext (Sky 200)
        lblBrandSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lblBrandSub);
        
        sidebar.add(Box.createVerticalStrut(30));

        // Navigation Buttons
        sidebar.add(createSidebarBtn("Dashboard", "dashboard"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createSidebarBtn("Manage Outages", "outages"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createSidebarBtn("Dispatch Technicians", "technicians"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createSidebarBtn("Event Notifications", "notifications"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createSidebarBtn("Incident Timeline Logs", "history"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createSidebarBtn("Analytics & Reliability", "analytics"));
        
        sidebar.add(Box.createVerticalGlue());

        // Session Information at bottom of sidebar
        JLabel lblUserTitle = new JLabel("Current Session:");
        lblUserTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblUserTitle.setForeground(new Color(186, 230, 253)); // Sky 200
        lblUserTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lblUserTitle);

        JLabel lblUserInfo = new JLabel(currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        lblUserInfo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUserInfo.setForeground(new Color(224, 242, 254)); // Light baby blue
        lblUserInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lblUserInfo);
        
        sidebar.add(Box.createVerticalStrut(15));

        JButton btnLogout = new JButton("Logout System");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.setBackground(new Color(239, 68, 68)); // Red 500
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> handleLogout());
        sidebar.add(btnLogout);

        mainContainer.add(sidebar, BorderLayout.WEST);

        // 2. Right Card Panel
        cardPanel = new JPanel(new CardLayout());
        cardPanel.setBackground(new Color(241, 245, 249));

        // Create the views
        outagePanel = new OutageManagementFrame(currentUser);
        technicianPanel = new TechnicianManagementFrame(currentUser);
        notificationPanel = new NotificationFrame();
        historyPanel = new HistoryFrame();
        statisticsPanel = new StatisticsFrame();

        // Add views to cardPanel
        cardPanel.add(createDashboardPanel(), "dashboard");
        cardPanel.add(outagePanel, "outages");
        cardPanel.add(technicianPanel, "technicians");
        cardPanel.add(notificationPanel, "notifications");
        cardPanel.add(historyPanel, "history");
        cardPanel.add(statisticsPanel, "analytics");

        mainContainer.add(cardPanel, BorderLayout.CENTER);

        setContentPane(mainContainer);
    }

    private JButton createSidebarBtn(String label, final String cardName) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(new Color(224, 242, 254)); // Soft Baby Blue text (Sky 100)
        btn.setBackground(new Color(6, 78, 59)); // Forest Green 900
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(210, 40));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(4, 120, 87)); // Emerald Green 700 Hover
                btn.setForeground(Color.WHITE);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(6, 78, 59)); // Forest Green 900
                btn.setForeground(new Color(224, 242, 254));
            }
        });

        btn.addActionListener(e -> {
            CardLayout cl = (CardLayout) cardPanel.getLayout();
            cl.show(cardPanel, cardName);
            // Refresh the target panel data
            if (cardName.equals("dashboard")) {
                refreshDashboardData();
            } else if (cardName.equals("outages")) {
                outagePanel.refreshData();
            } else if (cardName.equals("technicians")) {
                technicianPanel.refreshData();
            } else if (cardName.equals("notifications")) {
                notificationPanel.refreshData();
            } else if (cardName.equals("history")) {
                historyPanel.refreshData();
            } else if (cardName.equals("analytics")) {
                statisticsPanel.refreshData();
            }
        });

        return btn;
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 249, 255)); // Soft Baby Blue (Sky 50)
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Welcome / Greeting Banner
        JPanel greetingPanel = new JPanel(new BorderLayout());
        greetingPanel.setBackground(new Color(240, 249, 255));
        greetingPanel.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        JLabel lblGreeting = new JLabel("System Operations Center");
        lblGreeting.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblGreeting.setForeground(new Color(21, 128, 61)); // Forest Green
        
        JLabel lblTime = new JLabel("Status: Operations Normal | Real-time Stream Active");
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTime.setForeground(new Color(14, 165, 233)); // Baby Blue
        
        greetingPanel.add(lblGreeting, BorderLayout.WEST);
        greetingPanel.add(lblTime, BorderLayout.EAST);
        panel.add(greetingPanel, BorderLayout.NORTH);

        // Summary Cards Center Panel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(new Color(240, 249, 255));

        // Summary metrics grid
        JPanel statsGrid = new JPanel(new GridLayout(1, 5, 15, 0));
        statsGrid.setBackground(new Color(240, 249, 255));
        statsGrid.setPreferredSize(new Dimension(1000, 100));

        // 1. Total Outages
        JPanel cardTotal = createStatCard("Total Outages", lblTotalOutages = new JLabel("0"), new Color(14, 165, 233)); // Baby Blue
        // 2. Active Outages
        JPanel cardActive = createStatCard("Active Outages", lblActiveOutages = new JLabel("0"), new Color(245, 158, 11));
        // 3. Resolved Outages
        JPanel cardResolved = createStatCard("Resolved Outages", lblResolvedOutages = new JLabel("0"), new Color(16, 185, 129)); // Forest Green
        // 4. Critical Outages
        JPanel cardCritical = createStatCard("Critical Outages", lblCriticalOutages = new JLabel("0"), new Color(239, 68, 68));
        // 5. Available Techs
        JPanel cardTechs = createStatCard("Available Teams", lblAvailableTechs = new JLabel("0"), new Color(13, 148, 136)); // Green-Teal

        statsGrid.add(cardTotal);
        statsGrid.add(cardActive);
        statsGrid.add(cardResolved);
        statsGrid.add(cardCritical);
        statsGrid.add(cardTechs);

        centerPanel.add(statsGrid);
        centerPanel.add(Box.createVerticalStrut(20));

        // Recent tables section (Split Pane)
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        tablesPanel.setBackground(new Color(248, 250, 252));

        // Recent Outages
        JPanel recentOutagesPanel = new JPanel(new BorderLayout());
        recentOutagesPanel.setBackground(Color.WHITE);
        recentOutagesPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblRecentOutagesTitle = new JLabel("Recent Outage Incidents (Top 5)");
        lblRecentOutagesTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblRecentOutagesTitle.setForeground(new Color(15, 23, 42));
        lblRecentOutagesTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        recentOutagesPanel.add(lblRecentOutagesTitle, BorderLayout.NORTH);

        String[] recentCols = {"ID", "Area Name", "Cause", "Priority", "Status"};
        recentOutagesModel = new DefaultTableModel(recentCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tblRecentOutages = new JTable(recentOutagesModel);
        tblRecentOutages.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblRecentOutages.setRowHeight(24);
        tblRecentOutages.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblRecentOutages.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrollOutages = new JScrollPane(tblRecentOutages);
        scrollOutages.setBorder(null);
        recentOutagesPanel.add(scrollOutages, BorderLayout.CENTER);

        // Recent Notifications
        JPanel notificationsPanel = new JPanel(new BorderLayout());
        notificationsPanel.setBackground(Color.WHITE);
        notificationsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblRecentNotifTitle = new JLabel("Recent Event Log Stream");
        lblRecentNotifTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblRecentNotifTitle.setForeground(new Color(15, 23, 42));
        lblRecentNotifTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        notificationsPanel.add(lblRecentNotifTitle, BorderLayout.NORTH);

        notificationsListModel = new DefaultListModel<>();
        listRecentNotifications = new JList<>(notificationsListModel);
        listRecentNotifications.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        listRecentNotifications.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(new EmptyBorder(4, 4, 4, 4));
                String text = (String) value;
                if (text.contains("CRITICAL")) {
                    label.setForeground(new Color(239, 68, 68)); // Red for critical alert
                    label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                } else if (text.contains("Restored")) {
                    label.setForeground(new Color(16, 185, 129)); // Green for restored
                } else {
                    label.setForeground(new Color(71, 85, 105)); // Slate 600
                }
                return label;
            }
        });
        JScrollPane scrollNotifications = new JScrollPane(listRecentNotifications);
        scrollNotifications.setBorder(null);
        notificationsPanel.add(scrollNotifications, BorderLayout.CENTER);

        tablesPanel.add(recentOutagesPanel);
        tablesPanel.add(notificationsPanel);

        centerPanel.add(tablesPanel);
        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatCard(String title, JLabel lblValue, Color accentColor) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        
        // Dynamically style background cards to green & baby blue theme
        if (title.contains("Total") || title.contains("Available") || title.contains("Teams")) {
            card.setBackground(new Color(224, 242, 254)); // Baby Blue (Sky 100)
        } else if (title.contains("Resolved")) {
            card.setBackground(new Color(220, 252, 231)); // Soft Green (Green 100)
        } else {
            card.setBackground(Color.WHITE);
        }

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor), // Left accent line
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(226, 232, 240)),
                        new EmptyBorder(12, 15, 12, 15)
                )
        ));

        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTitle.setForeground(new Color(71, 85, 105)); // Slate 600

        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblValue.setForeground(new Color(15, 23, 42)); // Slate 900

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        return card;
    }

    private void refreshDashboardData() {
        // Fetch counters from database
        List<Outage> outages = outageDAO.getAllOutages();
        int total = outages.size();
        int active = 0;
        int resolved = 0;
        int critical = 0;

        for (Outage o : outages) {
            if ("POWER_RESTORED".equals(o.getStatus())) {
                resolved++;
            } else {
                active++;
                if ("CRITICAL".equalsIgnoreCase(o.getPriority())) {
                    critical++;
                }
            }
        }

        int availableTechs = technicianDAO.getAvailableTechnicians().size();

        lblTotalOutages.setText(String.valueOf(total));
        lblActiveOutages.setText(String.valueOf(active));
        lblResolvedOutages.setText(String.valueOf(resolved));
        lblCriticalOutages.setText(String.valueOf(critical));
        lblAvailableTechs.setText(String.valueOf(availableTechs));

        // Populate recent outages (top 5)
        recentOutagesModel.setRowCount(0);
        List<Outage> recent = outageDAO.getRecentOutages(5);
        for (Outage o : recent) {
            recentOutagesModel.addRow(new Object[]{
                "#" + o.getOutageId(),
                o.getAreaName(),
                o.getCause(),
                o.getPriority(),
                o.getStatus()
            });
        }

        // Populate recent notifications (top 8)
        notificationsListModel.clear();
        List<Notification> recentNotifs = notificationDAO.getRecentNotifications(8);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        for (Notification n : recentNotifs) {
            String timeStr = sdf.format(n.getCreatedTime());
            notificationsListModel.addElement("[" + timeStr + "] " + n.getMessage());
        }
    }

    private void handleLogout() {
        int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout of the system?", 
                "Confirm Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            this.dispose();
            // Re-open login frame
            SwingUtilities.invokeLater(() -> {
                LoginFrame login = new LoginFrame();
                login.setVisible(true);
            });
        }
    }

    private ImageIcon getLogoIcon(int width, int height) {
        try {
            java.net.URL imgUrl = getClass().getResource("/tge_logo.png");
            ImageIcon icon = null;
            if (imgUrl != null) {
                icon = new ImageIcon(imgUrl);
            } else {
                java.io.File file = new java.io.File("src/main/resources/tge_logo.png");
                if (file.exists()) {
                    icon = new ImageIcon(file.getAbsolutePath());
                }
            }
            if (icon != null) {
                Image img = icon.getImage();
                Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImg);
            }
        } catch (Exception e) {
            System.err.println("Could not load logo in Dashboard: " + e.getMessage());
        }
        return null;
    }
}
