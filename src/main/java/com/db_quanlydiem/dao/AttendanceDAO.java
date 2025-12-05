package com.db_quanlydiem.dao;

import com.db_quanlydiem.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;

public class AttendanceDAO {

    // Hàm lưu (hoặc cập nhật) điểm danh cho 1 sinh viên
    // Sử dụng cú pháp "ON DUPLICATE KEY UPDATE" để tự động sửa nếu đã có dữ liệu ngày hôm đó
    public boolean saveOrUpdateAttendance(String studentID, String classID, LocalDate date, boolean isPresent, String note) {
        String sql = "INSERT INTO ATTENDANCE (StudentID, CourseClassID, AttendanceDate, IsPresent, AttendanceNote) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE IsPresent = ?, AttendanceNote = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Phần INSERT
            ps.setString(1, studentID);
            ps.setString(2, classID);
            ps.setDate(3, java.sql.Date.valueOf(date));
            ps.setBoolean(4, isPresent);
            ps.setString(5, note);

            // Phần UPDATE (nếu trùng khóa chính UNIQUE)
            ps.setBoolean(6, isPresent);
            ps.setString(7, note);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Hàm kiểm tra trạng thái điểm danh của SV vào một ngày cụ thể (để hiển thị lại khi chọn ngày cũ)
    // Trả về: 1 (Có mặt), 0 (Vắng), -1 (Chưa điểm danh)
    public int getAttendanceStatus(String studentID, String classID, LocalDate date) {
        String sql = "SELECT IsPresent FROM ATTENDANCE WHERE StudentID = ? AND CourseClassID = ? AND AttendanceDate = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentID);
            ps.setString(2, classID);
            ps.setDate(3, java.sql.Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("IsPresent") ? 1 : 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Chưa có dữ liệu
    }

    // Hàm lấy ghi chú điểm danh (nếu có)
    public String getAttendanceNote(String studentID, String classID, LocalDate date) {
        String sql = "SELECT AttendanceNote FROM ATTENDANCE WHERE StudentID = ? AND CourseClassID = ? AND AttendanceDate = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentID);
            ps.setString(2, classID);
            ps.setDate(3, java.sql.Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("AttendanceNote");
        } catch (SQLException e) { e.printStackTrace(); }
        return "";
    }
}