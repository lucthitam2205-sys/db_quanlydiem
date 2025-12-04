package com.db_quanlydiem.dao;

import com.db_quanlydiem.DatabaseConnection;
import com.db_quanlydiem.model.Attendance;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttendanceDAO {

    // 1. THÊM BẢN GHI ĐIỂM DANH MỚI VÀ TRẢ VỀ ID (ĐÃ SỬA ĐỔI)
    /**
     * Thêm bản ghi điểm danh và trả về ID tự tăng của bản ghi.
     * @param att Đối tượng Attendance
     * @return AttendanceID mới được tạo, hoặc 0 nếu thất bại.
     */
    public int addAttendanceAndGetId(Attendance att) {
        String sql = "INSERT INTO ATTENDANCE (StudentID, CourseClassID, AttendanceDate, IsPresent, AttendanceNote) VALUES (?, ?, ?, ?, ?)";
        // Thêm Statement.RETURN_GENERATED_KEYS để lấy ID tự tăng
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, att.getStudentID());
            pstmt.setString(2, att.getCourseClassID());
            pstmt.setDate(3, att.getAttendanceDate());
            pstmt.setBoolean(4, att.isPresent());
            pstmt.setString(5, att.getNote());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1); // Trả về ID mới
                    }
                }
            }
        } catch (SQLException e) {
            // Nếu lỗi là do lỗi UNIQUE (bản ghi đã tồn tại), ta xử lý trong Controller
            e.printStackTrace();
        }
        return 0; // Trả về 0 nếu thất bại
    }

    // 2. Cập nhật bản ghi điểm danh đã tồn tại (Giữ nguyên)
    public boolean updateAttendance(Attendance att) {
        // Ta sử dụng StudentID, CourseClassID, AttendanceDate làm khóa kết hợp
        String sql = "UPDATE ATTENDANCE SET IsPresent = ?, AttendanceNote = ? WHERE StudentID = ? AND CourseClassID = ? AND AttendanceDate = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, att.isPresent());
            pstmt.setString(2, att.getNote());
            pstmt.setString(3, att.getStudentID());
            pstmt.setString(4, att.getCourseClassID());
            pstmt.setDate(5, att.getAttendanceDate());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 3. Kiểm tra xem bản ghi điểm danh đã tồn tại hay chưa (Giữ nguyên)
    public boolean isAttendanceExists(String studentID, String courseClassID, Date date) {
        String sql = "SELECT 1 FROM ATTENDANCE WHERE StudentID = ? AND CourseClassID = ? AND AttendanceDate = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentID);
            pstmt.setString(2, courseClassID);
            pstmt.setDate(3, date);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 4. Lấy tất cả bản ghi điểm danh cho một buổi cụ thể (Giữ nguyên)
    public List<Attendance> getAttendanceByClassAndDate(String courseClassID, Date date) {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT * FROM ATTENDANCE WHERE CourseClassID = ? AND AttendanceDate = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, courseClassID);
            pstmt.setDate(2, date);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Attendance(
                        rs.getInt("AttendanceID"),
                        rs.getString("StudentID"),
                        rs.getString("CourseClassID"),
                        rs.getDate("AttendanceDate"),
                        rs.getBoolean("IsPresent"),
                        rs.getString("AttendanceNote")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // 5. LẤY SỐ BUỔI VẮNG CỦA TỪNG SINH VIÊN TRONG LỚP (Hàm cần cho Dashboard) (Giữ nguyên)
    public Map<String, Integer> getAbsentCountsByClass(String courseClassID) {
        Map<String, Integer> absentCounts = new HashMap<>();
        String sql = "SELECT StudentID, COUNT(*) AS AbsentCount FROM ATTENDANCE " +
                "WHERE CourseClassID = ? AND IsPresent = FALSE GROUP BY StudentID";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, courseClassID);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                absentCounts.put(rs.getString("StudentID"), rs.getInt("AbsentCount"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return absentCounts;
    }
}