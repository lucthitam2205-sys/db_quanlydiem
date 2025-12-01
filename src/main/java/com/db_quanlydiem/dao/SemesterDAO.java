package com.db_quanlydiem.dao;

import com.db_quanlydiem.DatabaseConnection;
import com.db_quanlydiem.model.Semester;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SemesterDAO {

    // 1. Lấy tất cả học kỳ
    public List<Semester> getAllSemesters() {
        List<Semester> list = new ArrayList<>();
        String sql = "SELECT * FROM Semester ORDER BY StartDate DESC"; // Sắp xếp kỳ mới nhất lên đầu
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Semester(
                        rs.getString("SemesterID"),
                        rs.getString("SemesterName"),
                        rs.getDate("StartDate"),
                        rs.getDate("EndDate")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // 2. Thêm học kỳ mới
    public boolean addSemester(Semester s) {
        String sql = "INSERT INTO Semester (SemesterID, SemesterName, StartDate, EndDate) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, s.getSemesterID());
            pstmt.setString(2, s.getSemesterName());
            pstmt.setDate(3, s.getStartDate());
            pstmt.setDate(4, s.getEndDate());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 3. Cập nhật học kỳ
    public boolean updateSemester(Semester s) {
        String sql = "UPDATE Semester SET SemesterName=?, StartDate=?, EndDate=? WHERE SemesterID=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, s.getSemesterName());
            pstmt.setDate(2, s.getStartDate());
            pstmt.setDate(3, s.getEndDate());
            pstmt.setString(4, s.getSemesterID());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 4. Xóa học kỳ
    public boolean deleteSemester(String id) {
        String sql = "DELETE FROM Semester WHERE SemesterID=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 5. Tìm kiếm học kỳ
    public List<Semester> searchSemester(String keyword) {
        List<Semester> list = new ArrayList<>();
        String sql = "SELECT * FROM Semester WHERE SemesterID LIKE ? OR SemesterName LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String k = "%" + keyword + "%";
            pstmt.setString(1, k);
            pstmt.setString(2, k);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Semester(
                        rs.getString("SemesterID"),
                        rs.getString("SemesterName"),
                        rs.getDate("StartDate"),
                        rs.getDate("EndDate")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}