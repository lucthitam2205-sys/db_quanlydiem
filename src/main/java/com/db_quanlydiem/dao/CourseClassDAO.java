package com.db_quanlydiem.dao;

import com.db_quanlydiem.DatabaseConnection;
import com.db_quanlydiem.model.CourseClass;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseClassDAO {

    // 1. Lấy tất cả lớp học phần
    public List<CourseClass> getAllCourseClasses() {
        List<CourseClass> list = new ArrayList<>();
        String sql = "SELECT * FROM CourseClass";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new CourseClass(
                        rs.getString("CourseClassId"),
                        rs.getString("SubjectId"),
                        rs.getString("ClassName"),
                        rs.getInt("SubjectCredits"),
                        rs.getString("SemesterID"),
                        rs.getString("ProfessorID"),
                        rs.getInt("CourseCapacity")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // 2. Thêm lớp mới
    public boolean addCourseClass(CourseClass cc) {
        String sql = "INSERT INTO CourseClass (CourseClassId, SubjectId, ClassName, SubjectCredits, SemesterID, ProfessorID, CourseCapacity) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cc.getCourseClassId());
            pstmt.setString(2, cc.getSubjectId());
            pstmt.setString(3, cc.getClassName());
            pstmt.setInt(4, cc.getSubjectCredits());
            pstmt.setString(5, cc.getSemesterID());
            pstmt.setString(6, cc.getProfessorID());
            pstmt.setInt(7, cc.getCourseCapacity());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 3. Cập nhật lớp
    public boolean updateCourseClass(CourseClass cc) {
        String sql = "UPDATE CourseClass SET SubjectId=?, ClassName=?, SubjectCredits=?, SemesterID=?, ProfessorID=?, CourseCapacity=? WHERE CourseClassId=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cc.getSubjectId());
            pstmt.setString(2, cc.getClassName());
            pstmt.setInt(3, cc.getSubjectCredits());
            pstmt.setString(4, cc.getSemesterID());
            pstmt.setString(5, cc.getProfessorID());
            pstmt.setInt(6, cc.getCourseCapacity());
            pstmt.setString(7, cc.getCourseClassId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 4. Xóa lớp
    public boolean deleteCourseClass(String id) {
        String sql = "DELETE FROM CourseClass WHERE CourseClassId=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 5. Tìm kiếm
    public List<CourseClass> searchCourseClass(String keyword) {
        List<CourseClass> list = new ArrayList<>();
        String sql = "SELECT * FROM CourseClass WHERE CourseClassId LIKE ? OR SubjectId LIKE ? OR ClassName LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String k = "%" + keyword + "%";
            pstmt.setString(1, k);
            pstmt.setString(2, k);
            pstmt.setString(3, k);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new CourseClass(
                        rs.getString("CourseClassId"),
                        rs.getString("SubjectId"),
                        rs.getString("ClassName"),
                        rs.getInt("SubjectCredits"),
                        rs.getString("SemesterID"),
                        rs.getString("ProfessorID"),
                        rs.getInt("CourseCapacity")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}