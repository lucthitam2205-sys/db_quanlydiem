package com.db_quanlydiem.dao;

import com.db_quanlydiem.DatabaseConnection;
import com.db_quanlydiem.model.Student;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    // Lấy danh sách toàn bộ sinh viên
    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM Student";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Student s = new Student(
                        rs.getString("StudentID"),
                        rs.getString("StudentName"),
                        rs.getDate("StudentDOB"),
                        rs.getString("StudentGender"),
                        rs.getString("StudentMajor"),
                        rs.getString("StudentEmail"),
                        rs.getString("StudentPhone"),
                        rs.getString("StudentHometown"),
                        rs.getString("ParentName"),
                        rs.getString("ParentPhone"),
                        rs.getString("StudentStatus")
                );
                list.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    public boolean updateStudent(Student s) {
        String sql = "UPDATE Student SET StudentName=?, StudentDOB=?, StudentGender=?, StudentMajor=?, " +
                "StudentEmail=?, StudentPhone=?, StudentHometown=?, ParentName=?, ParentPhone=?, StudentStatus=? " +
                "WHERE StudentID=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, s.getStudentName());
            pstmt.setDate(2, s.getStudentDOB());
            pstmt.setString(3, s.getStudentGender());
            pstmt.setString(4, s.getStudentMajor());
            pstmt.setString(5, s.getStudentEmail());
            pstmt.setString(6, s.getStudentPhone());
            pstmt.setString(7, s.getStudentHometown());
            pstmt.setString(8, s.getParentName());
            pstmt.setString(9, s.getParentPhone());
            pstmt.setString(10, s.getStudentStatus());
            // Điều kiện WHERE (StudentID không đổi)
            pstmt.setString(11, s.getStudentID());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // Thêm sinh viên mới
    public boolean addStudent(Student s) {
        String sql = "INSERT INTO Student (StudentID, StudentName, StudentDOB, StudentGender, StudentMajor, " +
                "StudentEmail, StudentPhone, StudentHometown, ParentName, ParentPhone, StudentStatus) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, s.getStudentID());
            pstmt.setString(2, s.getStudentName());
            pstmt.setDate(3, s.getStudentDOB());
            pstmt.setString(4, s.getStudentGender());
            pstmt.setString(5, s.getStudentMajor());
            pstmt.setString(6, s.getStudentEmail());
            pstmt.setString(7, s.getStudentPhone());
            pstmt.setString(8, s.getStudentHometown());
            pstmt.setString(9, s.getParentName());
            pstmt.setString(10, s.getParentPhone());
            pstmt.setString(11, s.getStudentStatus());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Xóa sinh viên
    public boolean deleteStudent(String studentID) {
        String sql = "DELETE FROM Student WHERE StudentID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentID);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Tìm kiếm sinh viên theo tên hoặc mã
    public List<Student> searchStudent(String keyword) {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM Student WHERE StudentID LIKE ? OR StudentName LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Student(
                        rs.getString("StudentID"),
                        rs.getString("StudentName"),
                        rs.getDate("StudentDOB"),
                        rs.getString("StudentGender"),
                        rs.getString("StudentMajor"),
                        rs.getString("StudentEmail"),
                        rs.getString("StudentPhone"),
                        rs.getString("StudentHometown"),
                        rs.getString("ParentName"),
                        rs.getString("ParentPhone"),
                        rs.getString("StudentStatus")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}