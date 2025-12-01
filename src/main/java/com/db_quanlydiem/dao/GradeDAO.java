package com.db_quanlydiem.dao;

import com.db_quanlydiem.DatabaseConnection;
import com.db_quanlydiem.model.Grade;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GradeDAO {

    // Lấy bảng điểm của một lớp học phần cụ thể
    public List<Grade> getGradesByClass(String courseClassID) {
        List<Grade> list = new ArrayList<>();
        String sql = "SELECT * FROM Grade WHERE CourseClassID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, courseClassID);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(new Grade(
                        rs.getString("StudentID"),
                        rs.getString("CourseClassID"),
                        rs.getString("SemesterID"),
                        rs.getDouble("GradeAssessment1"),
                        rs.getDouble("GradeAssessment2"),
                        rs.getDouble("GradeFinal"),
                        rs.getDouble("GradeAverage"),
                        rs.getString("GradeNote")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Đăng ký học phần (Tạo dòng điểm rỗng ban đầu)
    public boolean registerCourse(String studentID, String courseClassID, String semesterID) {
        String sql = "INSERT INTO Grade (StudentID, CourseClassID, SemesterID, GradeAssessment1, GradeAssessment2, GradeFinal, GradeAverage) VALUES (?, ?, ?, 0, 0, 0, 0)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentID);
            pstmt.setString(2, courseClassID);
            pstmt.setString(3, semesterID);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Cập nhật điểm số (Chức năng nhập điểm của GV)
    public boolean updateGrade(Grade g) {
        // Tự động tính điểm trung bình trước khi lưu (Ví dụ: 30% - 20% - 50%)
        double avg = (g.getGradeAssessment1() * 0.3) + (g.getGradeAssessment2() * 0.2) + (g.getGradeFinal() * 0.5);

        String sql = "UPDATE Grade SET GradeAssessment1 = ?, GradeAssessment2 = ?, GradeFinal = ?, GradeAverage = ?, GradeNote = ? WHERE StudentID = ? AND CourseClassID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, g.getGradeAssessment1());
            pstmt.setDouble(2, g.getGradeAssessment2());
            pstmt.setDouble(3, g.getGradeFinal());
            pstmt.setDouble(4, avg); // Lưu điểm trung bình đã tính
            pstmt.setString(5, g.getGradeNote());
            pstmt.setString(6, g.getStudentID());
            pstmt.setString(7, g.getCourseClassID());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}