package com.db_quanlydiem.dao;

import com.db_quanlydiem.DatabaseConnection;
import com.db_quanlydiem.model.AuditLog;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAO {

    // Ghi log (Đã có)
    public void addLog(AuditLog log) {
        String sql = "INSERT INTO AuditLog (Username, ActionType, Description, LogTime) VALUES (?, ?, ?, NOW())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, log.getUsername());
            ps.setString(2, log.getActionType());
            ps.setString(3, log.getDescription());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- HÀM MỚI: Lấy toàn bộ lịch sử ---
    public List<AuditLog> getAllLogs() {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM AuditLog ORDER BY LogTime DESC"; // Mới nhất lên đầu
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new AuditLog(
                        rs.getInt("LogID"),
                        rs.getString("Username"),
                        rs.getString("ActionType"),
                        rs.getString("Description"),
                        rs.getTimestamp("LogTime")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // Tìm kiếm log
    public List<AuditLog> searchLogs(String keyword) {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM AuditLog WHERE Username LIKE ? OR Description LIKE ? ORDER BY LogTime DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String k = "%" + keyword + "%";
            ps.setString(1, k);
            ps.setString(2, k);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new AuditLog(
                        rs.getInt("LogID"),
                        rs.getString("Username"),
                        rs.getString("ActionType"),
                        rs.getString("Description"),
                        rs.getTimestamp("LogTime")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}