package main;

import com.formdev.flatlaf.FlatLightLaf;
import database.DBConnection;
import ui.LoginFrame;

import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        // 1. Setup flatlaf theme for modern, premium look & feel
        try {
            FlatLightLaf.setup();
            // Styling tweaks for standard JTable and headers
            UIManager.put("TableHeader.font", new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
            UIManager.put("Table.font", new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf theme. Reverting to system default.");
        }

        // 2. Validate DB connection before showing the UI
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Connection to MySQL Database successful.");
            } else {
                throw new SQLException("Connection was closed or null.");
            }
        } catch (SQLException e) {
            System.err.println("CRITICAL ERROR: Cannot connect to MySQL Database at localhost:3306.");
            System.err.println("Ensure MySQL server is running, database 'powerwatch' is initialized, and root user credentials are 'root/root'.");
            JOptionPane.showMessageDialog(null, 
                    "Could not connect to the MySQL database.\nEnsure MySQL is active and database 'powerwatch' is seeded.", 
                    "Database Connection Error", 
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // 3. Launch Login screen in the event dispatch thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
