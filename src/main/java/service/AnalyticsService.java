package service;

import database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class AnalyticsService {

    public Map<String, Integer> getMonthlyOutages() {
        Map<String, Integer> map = new LinkedHashMap<>();
        // Query to group by month name or year-month
        String sql = "SELECT DATE_FORMAT(report_time, '%M %Y') as month_yr, COUNT(*) as count " +
                     "FROM outage_reports " +
                     "GROUP BY DATE_FORMAT(report_time, '%Y-%m'), DATE_FORMAT(report_time, '%M %Y') " +
                     "ORDER BY DATE_FORMAT(report_time, '%Y-%m') ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("month_yr"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public Map<String, Integer> getOutagesByArea() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT area_name, COUNT(*) as count " +
                     "FROM outage_reports " +
                     "GROUP BY area_name " +
                     "ORDER BY count DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("area_name"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public Map<String, Integer> getOutagesByCause() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT cause, COUNT(*) as count " +
                     "FROM outage_reports " +
                     "GROUP BY cause " +
                     "ORDER BY count DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("cause"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public Map<String, Double> getAverageRestorationTimeByCause() {
        Map<String, Double> map = new LinkedHashMap<>();
        String sql = "SELECT cause, AVG(TIMESTAMPDIFF(SECOND, report_time, actual_restore_time)) / 3600.0 as avg_hours " +
                     "FROM outage_reports " +
                     "WHERE status = 'POWER_RESTORED' AND actual_restore_time IS NOT NULL " +
                     "GROUP BY cause " +
                     "ORDER BY avg_hours DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                double avg = rs.getDouble("avg_hours");
                // Round to 1 decimal place
                avg = Math.round(avg * 10.0) / 10.0;
                map.put(rs.getString("cause"), avg);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * Calculates Area Reliability Score using:
     * Reliability Score = 100 - (Outage Frequency Impact + Average Downtime Impact)
     * Outage Frequency Impact = Outage Count in Area * 3.0 (capped at 40)
     * Average Downtime Impact = Average downtime in hours * 2.0 (capped at 60)
     */
    public Map<String, Double> getAreaReliabilityScores() {
        Map<String, Double> scores = new LinkedHashMap<>();
        
        // Fetch all distinct areas represented in the infrastructures and outages
        Set<String> areas = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        
        // Seed some known areas from infrastructure
        String sqlInfra = "SELECT DISTINCT area_name FROM infrastructures";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlInfra);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                areas.add(rs.getString("area_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Add areas from outage reports
        String sqlOutages = "SELECT DISTINCT area_name FROM outage_reports";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlOutages);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                areas.add(rs.getString("area_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Calculate score for each area
        String sqlMetrics = "SELECT " +
                            "COUNT(*) as total_outages, " +
                            "COALESCE(AVG(TIMESTAMPDIFF(SECOND, report_time, COALESCE(actual_restore_time, NOW()))), 0) / 3600.0 as avg_downtime " +
                            "FROM outage_reports " +
                            "WHERE LOWER(area_name) = LOWER(?)";

        for (String area : areas) {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sqlMetrics)) {
                ps.setString(1, area);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt("total_outages");
                        double avgDowntime = rs.getDouble("avg_downtime");

                        double frequencyImpact = Math.min(count * 3.0, 40.0);
                        double downtimeImpact = Math.min(avgDowntime * 2.0, 60.0);

                        double score = 100.0 - (frequencyImpact + downtimeImpact);
                        // Round score to 1 decimal place and cap between 0 and 100
                        score = Math.max(0.0, Math.min(100.0, score));
                        score = Math.round(score * 10.0) / 10.0;

                        scores.put(area, score);
                    } else {
                        scores.put(area, 100.0);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                scores.put(area, 100.0);
            }
        }
        return scores;
    }
}
