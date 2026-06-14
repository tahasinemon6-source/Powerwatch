package ui;

import dao.OutageDAO;
import model.OutageHistory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class HistoryFrame extends JPanel {
    private JTable tblHistory;
    private DefaultTableModel tableModel;
    private JTextField txtSearchOutageId;
    private OutageDAO outageDAO;

    public HistoryFrame() {
        outageDAO = new OutageDAO();
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

        JLabel lblTitle = new JLabel("Outage Status Transition Log");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(30, 41, 59));

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setBackground(Color.WHITE);

        JLabel lblSearch = new JLabel("Outage ID:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSearch.setForeground(new Color(71, 85, 105));

        txtSearchOutageId = new JTextField(8);
        txtSearchOutageId.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JButton btnSearch = new JButton("Search Timeline");
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSearch.setBackground(new Color(71, 85, 105));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFocusPainted(false);
        btnSearch.addActionListener(e -> searchHistory());

        JButton btnReset = new JButton("Clear / Refresh");
        btnReset.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnReset.setBackground(new Color(37, 99, 235));
        btnReset.setForeground(Color.WHITE);
        btnReset.setFocusPainted(false);
        btnReset.addActionListener(e -> {
            txtSearchOutageId.setText("");
            refreshData();
        });

        filterPanel.add(lblSearch);
        filterPanel.add(txtSearchOutageId);
        filterPanel.add(btnSearch);
        filterPanel.add(btnReset);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(filterPanel, BorderLayout.EAST);

        // Table
        String[] columnNames = {"Log ID", "Outage ID", "Previous Status", "New Status", "Modified By", "Timestamp"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblHistory = new JTable(tableModel);
        tblHistory.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblHistory.setRowHeight(25);
        tblHistory.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblHistory.getTableHeader().setReorderingAllowed(false);

        // Columns sizing
        tblHistory.getColumnModel().getColumn(0).setPreferredWidth(80);
        tblHistory.getColumnModel().getColumn(1).setPreferredWidth(80);
        tblHistory.getColumnModel().getColumn(2).setPreferredWidth(180);
        tblHistory.getColumnModel().getColumn(3).setPreferredWidth(180);
        tblHistory.getColumnModel().getColumn(4).setPreferredWidth(120);
        tblHistory.getColumnModel().getColumn(5).setPreferredWidth(180);

        JScrollPane scrollPane = new JScrollPane(tblHistory);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void searchHistory() {
        String idText = txtSearchOutageId.getText().trim();
        if (idText.isEmpty()) {
            refreshData();
            return;
        }

        try {
            int outageId = Integer.parseInt(idText);
            tableModel.setRowCount(0);
            List<OutageHistory> list = outageDAO.getHistoryForOutage(outageId);
            if (list.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No history logs found for Outage ID #" + outageId, "Info", JOptionPane.INFORMATION_MESSAGE);
            }
            for (OutageHistory log : list) {
                tableModel.addRow(new Object[]{
                    log.getHistoryId(),
                    "#" + log.getOutageId(),
                    log.getOldStatus(),
                    log.getNewStatus(),
                    log.getUpdatedBy(),
                    log.getUpdateTime()
                });
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric Outage ID.", "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        List<OutageHistory> list = outageDAO.getAllHistory();
        for (OutageHistory log : list) {
            tableModel.addRow(new Object[]{
                log.getHistoryId(),
                "#" + log.getOutageId(),
                log.getOldStatus(),
                log.getNewStatus(),
                log.getUpdatedBy(),
                log.getUpdateTime()
            });
        }
    }
}
