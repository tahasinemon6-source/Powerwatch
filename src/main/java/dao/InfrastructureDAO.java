package dao;

import database.DBConnection;
import model.Infrastructure;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InfrastructureDAO {

    public boolean addInfrastructure(Infrastructure infra) {
        String sql = "INSERT INTO infrastructures (name, area_name, type) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, infra.getName());
            ps.setString(2, infra.getAreaName());
            ps.setString(3, infra.getType());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Infrastructure> getAllInfrastructures() {
        List<Infrastructure> list = new ArrayList<>();
        String sql = "SELECT * FROM infrastructures";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Infrastructure(
                    rs.getInt("infrastructure_id"),
                    rs.getString("name"),
                    rs.getString("area_name"),
                    rs.getString("type")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Infrastructure> getInfrastructuresByArea(String areaName) {
        List<Infrastructure> list = new ArrayList<>();
        String sql = "SELECT * FROM infrastructures WHERE LOWER(area_name) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, areaName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Infrastructure(
                        rs.getInt("infrastructure_id"),
                        rs.getString("name"),
                        rs.getString("area_name"),
                        rs.getString("type")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean isCriticalArea(String areaName) {
        String sql = "SELECT COUNT(*) FROM infrastructures WHERE LOWER(area_name) = LOWER(?)";
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
}
