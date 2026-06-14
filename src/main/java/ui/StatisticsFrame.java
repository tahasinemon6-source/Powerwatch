package ui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import service.AnalyticsService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

public class StatisticsFrame extends JPanel {
    private AnalyticsService analyticsService;
    private JPanel chartsContainer;
    private JTable tblReliability;
    private DefaultTableModel reliabilityModel;

    public StatisticsFrame() {
        analyticsService = new AnalyticsService();
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

        JLabel lblTitle = new JLabel("Analytics & Reliability Dashboard");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(30, 41, 59));

        JButton btnRefresh = new JButton("Refresh Analytics");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.setBackground(new Color(37, 99, 235));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> refreshData());

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnRefresh, BorderLayout.EAST);

        // Split Layout: Charts on top/left, Area Reliability Score on bottom/right
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(450);
        splitPane.setBorder(null);
        splitPane.setBackground(Color.WHITE);

        // Chart container (Grid layout with 4 charts)
        chartsContainer = new JPanel(new GridLayout(2, 2, 15, 15));
        chartsContainer.setBackground(Color.WHITE);
        splitPane.setTopComponent(chartsContainer);

        // Reliability Panel (bottom panel)
        JPanel reliabilityPanel = new JPanel(new BorderLayout());
        reliabilityPanel.setBackground(Color.WHITE);
        reliabilityPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JLabel lblReliabilityTitle = new JLabel("Area Reliability Scores");
        lblReliabilityTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblReliabilityTitle.setForeground(new Color(30, 41, 59));
        lblReliabilityTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        String[] cols = {"Area Name", "Reliability Score (%)", "Status Alert"};
        reliabilityModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tblReliability = new JTable(reliabilityModel);
        tblReliability.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblReliability.setRowHeight(25);
        tblReliability.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblReliability.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(tblReliability);
        scrollPane.setPreferredSize(new Dimension(800, 180));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        reliabilityPanel.add(lblReliabilityTitle, BorderLayout.NORTH);
        reliabilityPanel.add(scrollPane, BorderLayout.CENTER);

        splitPane.setBottomComponent(reliabilityPanel);

        add(headerPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }

    public void refreshData() {
        // Clear components and rebuild charts
        chartsContainer.removeAll();

        // 1. Monthly Outages Chart
        Map<String, Integer> monthlyData = analyticsService.getMonthlyOutages();
        DefaultCategoryDataset monthlyDataset = new DefaultCategoryDataset();
        monthlyData.forEach((k, v) -> monthlyDataset.addValue(v, "Outages", k));
        JFreeChart monthlyChart = ChartFactory.createBarChart(
                "Monthly Outages", "Month", "Number of Outages",
                monthlyDataset, PlotOrientation.VERTICAL, false, true, false
        );
        styleChart(monthlyChart);
        chartsContainer.add(new ChartPanel(monthlyChart));

        // 2. Outages by Area Chart
        Map<String, Integer> areaData = analyticsService.getOutagesByArea();
        DefaultCategoryDataset areaDataset = new DefaultCategoryDataset();
        areaData.forEach((k, v) -> areaDataset.addValue(v, "Outages", k));
        JFreeChart areaChart = ChartFactory.createBarChart(
                "Outages by Area", "Area", "Number of Outages",
                areaDataset, PlotOrientation.VERTICAL, false, true, false
        );
        styleChart(areaChart);
        chartsContainer.add(new ChartPanel(areaChart));

        // 3. Outage Causes Chart
        Map<String, Integer> causeData = analyticsService.getOutagesByCause();
        DefaultPieDataset causeDataset = new DefaultPieDataset();
        causeData.forEach(causeDataset::setValue);
        JFreeChart causeChart = ChartFactory.createPieChart(
                "Outage Causes Distribution", causeDataset, true, true, false
        );
        styleChart(causeChart);
        chartsContainer.add(new ChartPanel(causeChart));

        // 4. Average Restoration Time by Cause
        Map<String, Double> timeData = analyticsService.getAverageRestorationTimeByCause();
        DefaultCategoryDataset timeDataset = new DefaultCategoryDataset();
        timeData.forEach((k, v) -> timeDataset.addValue(v, "Hours", k));
        JFreeChart timeChart = ChartFactory.createBarChart(
                "Avg Restoration Time (Hours)", "Cause", "Hours",
                timeDataset, PlotOrientation.VERTICAL, false, true, false
        );
        styleChart(timeChart);
        chartsContainer.add(new ChartPanel(timeChart));

        chartsContainer.revalidate();
        chartsContainer.repaint();

        // Populate Reliability Score Table
        reliabilityModel.setRowCount(0);
        Map<String, Double> reliabilityScores = analyticsService.getAreaReliabilityScores();
        for (Map.Entry<String, Double> entry : reliabilityScores.entrySet()) {
            double score = entry.getValue();
            String status;
            if (score >= 90.0) {
                status = "Excellent (Highly Stable)";
            } else if (score >= 80.0) {
                status = "Good (Minor Outages)";
            } else if (score >= 60.0) {
                status = "Moderate (Needs Monitoring)";
            } else {
                status = "Poor (Unstable / High Downtime)";
            }
            reliabilityModel.addRow(new Object[]{
                entry.getKey(),
                score + "%",
                status
            });
        }
    }

    private void styleChart(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 14));
        chart.getTitle().setPaint(new Color(30, 41, 59));
    }
}
