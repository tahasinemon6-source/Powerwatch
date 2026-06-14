package ui;

import dao.UserDAO;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private UserDAO userDAO;

    public LoginFrame() {
        userDAO = new UserDAO();
        initUI();
    }

    private void initUI() {
        setTitle("PowerWatch - System Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 420);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main panel with a modern layout
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(new Color(224, 242, 254)); // Soft Baby Blue background (Sky 100)
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(224, 242, 254));
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        
        ImageIcon logoIcon = getLogoIcon(80, 80);
        if (logoIcon != null) {
            JLabel lblLogo = new JLabel(logoIcon);
            lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
            headerPanel.add(lblLogo);
            headerPanel.add(Box.createVerticalStrut(10));
        }
        
        JLabel lblTitle = new JLabel("POWERWATCH");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(21, 128, 61)); // Forest Green 700
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblSub = new JLabel("Emergency Outage & Repair Manager");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(71, 85, 105)); // Slate 600
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(lblTitle);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(lblSub);
        headerPanel.add(Box.createVerticalStrut(20));

        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        formPanel.setBackground(new Color(224, 242, 254));

        JLabel lblUsername = new JLabel("Username");
        lblUsername.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUsername.setForeground(new Color(21, 128, 61)); // Forest Green
        
        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsername.putClientProperty("JTextField.placeholderText", "Enter your username");

        JLabel lblPassword = new JLabel("Password");
        lblPassword.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPassword.setForeground(new Color(21, 128, 61)); // Forest Green

        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.putClientProperty("JTextField.placeholderText", "Enter your password");

        formPanel.add(lblUsername);
        formPanel.add(txtUsername);
        formPanel.add(lblPassword);
        formPanel.add(txtPassword);

        // Action Panel
        JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.setBackground(new Color(224, 242, 254));
        actionPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        btnLogin = new JButton("Login");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(new Color(14, 165, 233)); // Baby Blue / Sky 500
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.putClientProperty("JButton.buttonType", "roundRect");

        // Action listeners
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        // Trigger login on pressing Enter
        txtPassword.addActionListener(e -> handleLogin());
        txtUsername.addActionListener(e -> handleLogin());

        actionPanel.add(btnLogin, BorderLayout.CENTER);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(actionPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
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
            System.err.println("Could not load logo: " + e.getMessage());
        }
        return null;
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and Password cannot be empty.", 
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User user = userDAO.authenticate(username, password);
        if (user != null) {
            // Login successful
            JOptionPane.showMessageDialog(this, "Login successful. Welcome back, " + user.getUsername() + "!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            
            // Launch main dashboard
            SwingUtilities.invokeLater(() -> {
                DashboardFrame dashboard = new DashboardFrame(user);
                dashboard.setVisible(true);
            });
            this.dispose();
        } else {
            // Login failed
            JOptionPane.showMessageDialog(this, "Invalid username or password.",
                    "Authentication Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
