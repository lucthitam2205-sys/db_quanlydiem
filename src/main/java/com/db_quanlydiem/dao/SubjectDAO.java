package com.db_quanlydiem.dao;

import com.db_quanlydiem.DatabaseConnection;
import com.db_quanlydiem.model.Subject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubjectDAO {

    // 1. Lấy tất cả môn học
    public List<Subject> getAllSubjects() {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT * FROM Subject";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Subject(
                        rs.getString("SubjectId"),
                        rs.getString("SubjectName"),
                        rs.getInt("SubjectCredit")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // 2. Thêm môn học
    public boolean addSubject(Subject s) {
        String sql = "INSERT INTO Subject (SubjectId, SubjectName, SubjectCredit) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, s.getSubjectId());
            pstmt.setString(2, s.getSubjectName());
            pstmt.setInt(3, s.getSubjectCredit());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 3. Cập nhật môn học
    public boolean updateSubject(Subject s) {
        String sql = "UPDATE Subject SET SubjectName = ?, SubjectCredit = ? WHERE SubjectId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, s.getSubjectName());
            pstmt.setInt(2, s.getSubjectCredit());
            pstmt.setString(3, s.getSubjectId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 4. Xóa môn học
    public boolean deleteSubject(String id) {
        String sql = "DELETE FROM Subject WHERE SubjectId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 5. Tìm kiếm môn học
    public List<Subject> searchSubject(String keyword) {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT * FROM Subject WHERE SubjectId LIKE ? OR SubjectName LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String k = "%" + keyword + "%";
            pstmt.setString(1, k);
            pstmt.setString(2, k);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Subject(
                        rs.getString("SubjectId"),
                        rs.getString("SubjectName"),
                        rs.getInt("SubjectCredit")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}