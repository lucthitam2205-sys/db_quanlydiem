package com.db_quanlydiem.dao;

import com.db_quanlydiem.DatabaseConnection;
import com.db_quanlydiem.model.TuitionConfig;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TuitionConfigDAO {

    // 1. Lấy tất cả cấu hình
    public List<TuitionConfig> getAllConfigs() {
        List<TuitionConfig> list = new ArrayList<>();
        String sql = "SELECT * FROM TuitionConfig ORDER BY ConfigID DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new TuitionConfig(
                        rs.getInt("ConfigID"),
                        rs.getString("SemesterID"),
                        rs.getDouble("PricePerCredit"),
                        rs.getString("Description")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // 2. Lấy cấu hình theo học kỳ (Giữ lại để kiểm tra)
    public TuitionConfig getConfigBySemester(String semesterID) {
        String sql = "SELECT * FROM TuitionConfig WHERE SemesterID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, semesterID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new TuitionConfig(
                        rs.getInt("ConfigID"),
                        rs.getString("SemesterID"),
                        rs.getDouble("PricePerCredit"),
                        rs.getString("Description")
                );
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // 3. Thêm cấu hình mới
    public boolean addConfig(TuitionConfig tc) {
        String sql = "INSERT INTO TuitionConfig (SemesterID, PricePerCredit, Description) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tc.getSemesterID());
            pstmt.setDouble(2, tc.getPricePerCredit());
            pstmt.setString(3, tc.getDescription());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 4. Cập nhật cấu hình
    public boolean updateConfig(TuitionConfig tc) {
        String sql = "UPDATE TuitionConfig SET PricePerCredit=?, Description=? WHERE ConfigID=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, tc.getPricePerCredit());
            pstmt.setString(2, tc.getDescription());
            pstmt.setInt(3, tc.getConfigID());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 5. Xóa cấu hình
    public boolean deleteConfig(int id) {
        String sql = "DELETE FROM TuitionConfig WHERE ConfigID=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}