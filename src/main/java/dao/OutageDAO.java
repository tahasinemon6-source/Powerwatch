package dao;

import database.DBConnection;
import model.Outage;
import model.OutageHistory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OutageDAO {

    public int createOutage(Outage outage, String username) {
        String sql = "INSERT INTO outage_reports (area_name, outage_type, priority, cause, report_time, estimated_restore_time, status, assigned_technician_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        int generatedId = -1;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, outage.getAreaName());
            ps.setString(2, outage.getOutageType());
            ps.setString(3, outage.getPriority());
            ps.setString(4, outage.getCause());
            ps.setTimestamp(5, outage.getReportTime());
            ps.setTimestamp(6, outage.getEstimatedRestoreTime());
            ps.setString(7, outage.getStatus());
            if (outage.getAssignedTechnicianId() != null) {
                ps.setInt(8, outage.getAssignedTechnicianId());
            } else {
                ps.setNull(8, java.sql.Types.INTEGER);
            }

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getInt(1);
                        outage.setOutageId(generatedId);
                        // Log history
                        logStatusChange(conn, generatedId, "NONE", outage.getStatus(), username);
                        
                        // If technician is assigned, update their status to BUSY
                        if (outage.getAssignedTechnicianId() != null) {
                            updateTechnicianStatus(conn, outage.getAssignedTechnicianId(), "BUSY");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return generatedId;
    }

    public boolean updateOutage(Outage outage, String username) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Fetch current state of outage to check if status or technician has changed
            Outage oldOutage = getOutageById(outage.getOutageId());
            if (oldOutage == null) {
                conn.rollback();
                return false;
            }

            String sql = "UPDATE outage_reports SET area_name = ?, outage_type = ?, priority = ?, cause = ?, " +
                         "estimated_restore_time = ?, actual_restore_time = ?, status = ?, assigned_technician_id = ? " +
                         "WHERE outage_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, outage.getAreaName());
                ps.setString(2, outage.getOutageType());
                ps.setString(3, outage.getPriority());
                ps.setString(4, outage.getCause());
                ps.setTimestamp(5, outage.getEstimatedRestoreTime());
                ps.setTimestamp(6, outage.getActualRestoreTime());
                ps.setString(7, outage.getStatus());
                if (outage.getAssignedTechnicianId() != null) {
                    ps.setInt(8, outage.getAssignedTechnicianId());
                } else {
                    ps.setNull(8, java.sql.Types.INTEGER);
                }
                ps.setInt(9, outage.getOutageId());
                
                int updated = ps.executeUpdate();
                if (updated > 0) {
                    // Check if status changed
                    if (!oldOutage.getStatus().equals(outage.getStatus())) {
                        logStatusChange(conn, outage.getOutageId(), oldOutage.getStatus(), outage.getStatus(), username);
                    }

                    // Check technician changes
                    Integer oldTechId = oldOutage.getAssignedTechnicianId();
                    Integer newTechId = outage.getAssignedTechnicianId();

                    // If status changes to POWER_RESTORED, make technician AVAILABLE
                    if ("POWER_RESTORED".equalsIgnoreCase(outage.getStatus())) {
                        if (newTechId != null) {
                            updateTechnicianStatus(conn, newTechId, "AVAILABLE");
                        }
                    } else {
                        // Technician reassignment logic
                        if (oldTechId == null && newTechId != null) {
                            // Tech newly assigned
                            updateTechnicianStatus(conn, newTechId, "BUSY");
                        } else if (oldTechId != null && newTechId == null) {
                            // Tech removed
                            updateTechnicianStatus(conn, oldTechId, "AVAILABLE");
                        } else if (oldTechId != null && !oldTechId.equals(newTechId)) {
                            // Tech changed
                            updateTechnicianStatus(conn, oldTechId, "AVAILABLE");
                            updateTechnicianStatus(conn, newTechId, "BUSY");
                        }
                    }

                    conn.commit();
                    return true;
                }
            }
            conn.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public boolean deleteOutage(int outageId) {
        // We need to restore technician availability if deleted outage was active
        Outage outage = getOutageById(outageId);
        if (outage != null && outage.getAssignedTechnicianId() != null && !"POWER_RESTORED".equalsIgnoreCase(outage.getStatus())) {
            try (Connection conn = DBConnection.getConnection()) {
                updateTechnicianStatus(conn, outage.getAssignedTechnicianId(), "AVAILABLE");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        String sql = "DELETE FROM outage_reports WHERE outage_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, outageId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Outage getOutageById(int outageId) {
        String sql = "SELECT * FROM outage_reports WHERE outage_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, outageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Integer techId = rs.getInt("assigned_technician_id");
                    if (rs.wasNull()) {
                        techId = null;
                    }
                    return new Outage(
                        rs.getInt("outage_id"),
                        rs.getString("area_name"),
                        rs.getString("outage_type"),
                        rs.getString("priority"),
                        rs.getString("cause"),
                        rs.getTimestamp("report_time"),
                        rs.getTimestamp("estimated_restore_time"),
                        rs.getTimestamp("actual_restore_time"),
                        rs.getString("status"),
                        techId
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Outage> getAllOutages() {
        List<Outage> list = new ArrayList<>();
        String sql = "SELECT * FROM outage_reports ORDER BY report_time DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Integer techId = rs.getInt("assigned_technician_id");
                if (rs.wasNull()) {
                    techId = null;
                }
                list.add(new Outage(
                    rs.getInt("outage_id"),
                    rs.getString("area_name"),
                    rs.getString("outage_type"),
                    rs.getString("priority"),
                    rs.getString("cause"),
                    rs.getTimestamp("report_time"),
                    rs.getTimestamp("estimated_restore_time"),
                    rs.getTimestamp("actual_restore_time"),
                    rs.getString("status"),
                    techId
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Outage> getRecentOutages(int limit) {
        List<Outage> list = new ArrayList<>();
        String sql = "SELECT * FROM outage_reports ORDER BY report_time DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Integer techId = rs.getInt("assigned_technician_id");
                    if (rs.wasNull()) {
                        techId = null;
                    }
                    list.add(new Outage(
                        rs.getInt("outage_id"),
                        rs.getString("area_name"),
                        rs.getString("outage_type"),
                        rs.getString("priority"),
                        rs.getString("cause"),
                        rs.getTimestamp("report_time"),
                        rs.getTimestamp("estimated_restore_time"),
                        rs.getTimestamp("actual_restore_time"),
                        rs.getString("status"),
                        techId
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Outage> searchOutages(String area, String status, String dateStr) {
        List<Outage> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM outage_reports WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (area != null && !area.trim().isEmpty()) {
            sql.append(" AND LOWER(area_name) LIKE ?");
            params.add("%" + area.trim().toLowerCase() + "%");
        }
        if (status != null && !status.equalsIgnoreCase("ALL")) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            sql.append(" AND DATE(report_time) = ?");
            params.add(Date.valueOf(dateStr)); // expects YYYY-MM-DD
        }
        sql.append(" ORDER BY report_time DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Integer techId = rs.getInt("assigned_technician_id");
                    if (rs.wasNull()) {
                        techId = null;
                    }
                    list.add(new Outage(
                        rs.getInt("outage_id"),
                        rs.getString("area_name"),
                        rs.getString("outage_type"),
                        rs.getString("priority"),
                        rs.getString("cause"),
                        rs.getTimestamp("report_time"),
                        rs.getTimestamp("estimated_restore_time"),
                        rs.getTimestamp("actual_restore_time"),
                        rs.getString("status"),
                        techId
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean hasActiveOutageInArea(String areaName) {
        String sql = "SELECT COUNT(*) FROM outage_reports WHERE LOWER(area_name) = LOWER(?) AND status != 'POWER_RESTORED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, areaName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public double getAverageRestorationTimeHours(String cause) {
        String sql = "SELECT AVG(TIMESTAMPDIFF(SECOND, report_time, actual_restore_time)) / 3600.0 FROM outage_reports " +
                     "WHERE cause = ? AND status = 'POWER_RESTORED' AND actual_restore_time IS NOT NULL";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cause);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double avg = rs.getDouble(1);
                    if (!rs.wasNull()) {
                        return avg;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1.0; // returns negative if no historical data exists
    }

    public List<OutageHistory> getHistoryForOutage(int outageId) {
        List<OutageHistory> list = new ArrayList<>();
        String sql = "SELECT * FROM outage_history WHERE outage_id = ? ORDER BY update_time ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, outageId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new OutageHistory(
                        rs.getInt("history_id"),
                        rs.getInt("outage_id"),
                        rs.getString("old_status"),
                        rs.getString("new_status"),
                        rs.getString("updated_by"),
                        rs.getTimestamp("update_time")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<OutageHistory> getAllHistory() {
        List<OutageHistory> list = new ArrayList<>();
        String sql = "SELECT * FROM outage_history ORDER BY update_time DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new OutageHistory(
                    rs.getInt("history_id"),
                    rs.getInt("outage_id"),
                    rs.getString("old_status"),
                    rs.getString("new_status"),
                    rs.getString("updated_by"),
                    rs.getTimestamp("update_time")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private void logStatusChange(Connection conn, int outageId, String oldStatus, String newStatus, String username) throws SQLException {
        String sql = "INSERT INTO outage_history (outage_id, old_status, new_status, updated_by) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, outageId);
            ps.setString(2, oldStatus);
            ps.setString(3, newStatus);
            ps.setString(4, username == null ? "SYSTEM" : username);
            ps.executeUpdate();
        }
    }

    private void updateTechnicianStatus(Connection conn, int technicianId, String status) throws SQLException {
        String sql = "UPDATE technicians SET availability_status = ? WHERE technician_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, technicianId);
            ps.executeUpdate();
        }
    }
}
