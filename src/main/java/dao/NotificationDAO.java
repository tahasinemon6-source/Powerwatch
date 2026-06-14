package dao;

import database.DBConnection;
import model.Notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public boolean addNotification(Notification notif) {
        String sql = "INSERT INTO notifications (outage_id, message, created_time) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (notif.getOutageId() != null) {
                ps.setInt(1, notif.getOutageId());
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            ps.setString(2, notif.getMessage());
            ps.setTimestamp(3, notif.getCreatedTime());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Notification> getAllNotifications() {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications ORDER BY created_time DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Integer outageId = rs.getInt("outage_id");
                if (rs.wasNull()) {
                    outageId = null;
                }
                list.add(new Notification(
                    rs.getInt("notification_id"),
                    outageId,
                    rs.getString("message"),
                    rs.getTimestamp("created_time")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Notification> getRecentNotifications(int limit) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications ORDER BY created_time DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Integer outageId = rs.getInt("outage_id");
                    if (rs.wasNull()) {
                        outageId = null;
                    }
                    list.add(new Notification(
                        rs.getInt("notification_id"),
                        outageId,
                        rs.getString("message"),
                        rs.getTimestamp("created_time")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
