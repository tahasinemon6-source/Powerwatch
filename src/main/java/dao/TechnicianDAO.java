package dao;

import database.DBConnection;
import model.Technician;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TechnicianDAO {

    public boolean addTechnician(Technician tech) {
        String sql = "INSERT INTO technicians (technician_name, phone, team_name, availability_status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tech.getTechnicianName());
            ps.setString(2, tech.getPhone());
            ps.setString(3, tech.getTeamName());
            ps.setString(4, tech.getAvailabilityStatus());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateTechnician(Technician tech) {
        String sql = "UPDATE technicians SET technician_name = ?, phone = ?, team_name = ?, availability_status = ? WHERE technician_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tech.getTechnicianName());
            ps.setString(2, tech.getPhone());
            ps.setString(3, tech.getTeamName());
            ps.setString(4, tech.getAvailabilityStatus());
            ps.setInt(5, tech.getTechnicianId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteTechnician(int techId) {
        String sql = "DELETE FROM technicians WHERE technician_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, techId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Technician getTechnicianById(int techId) {
        String sql = "SELECT * FROM technicians WHERE technician_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, techId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Technician(
                        rs.getInt("technician_id"),
                        rs.getString("technician_name"),
                        rs.getString("phone"),
                        rs.getString("team_name"),
                        rs.getString("availability_status")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Technician> getAllTechnicians() {
        List<Technician> list = new ArrayList<>();
        String sql = "SELECT * FROM technicians";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Technician(
                    rs.getInt("technician_id"),
                    rs.getString("technician_name"),
                    rs.getString("phone"),
                    rs.getString("team_name"),
                    rs.getString("availability_status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Technician> getAvailableTechnicians() {
        List<Technician> list = new ArrayList<>();
        String sql = "SELECT * FROM technicians WHERE availability_status = 'AVAILABLE'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Technician(
                    rs.getInt("technician_id"),
                    rs.getString("technician_name"),
                    rs.getString("phone"),
                    rs.getString("team_name"),
                    rs.getString("availability_status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Technician> searchTechnicians(String query) {
        List<Technician> list = new ArrayList<>();
        String sql = "SELECT * FROM technicians WHERE technician_name LIKE ? OR team_name LIKE ? OR availability_status LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String formatQuery = "%" + query + "%";
            ps.setString(1, formatQuery);
            ps.setString(2, formatQuery);
            ps.setString(3, formatQuery);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Technician(
                        rs.getInt("technician_id"),
                        rs.getString("technician_name"),
                        rs.getString("phone"),
                        rs.getString("team_name"),
                        rs.getString("availability_status")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateAvailability(int techId, String status) {
        String sql = "UPDATE technicians SET availability_status = ? WHERE technician_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, techId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
