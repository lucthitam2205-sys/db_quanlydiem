package com.db_quanlydiem.dao;

import com.db_quanlydiem.DatabaseConnection;
import com.db_quanlydiem.model.ClassSchedule;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClassScheduleDAO {

    // 1. Lấy tất cả lịch học
    public List<ClassSchedule> getAllSchedules() {
        List<ClassSchedule> list = new ArrayList<>();
        String sql = "SELECT * FROM ClassSchedule ORDER BY CourseClassID, DayOfWeek";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new ClassSchedule(
                        rs.getInt("ScheduleID"),
                        rs.getString("CourseClassID"),
                        rs.getString("DayOfWeek"),
                        rs.getString("Shift"),
                        rs.getString("Room")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // 2. Thêm lịch học
    public boolean addSchedule(ClassSchedule cs) {
        String sql = "INSERT INTO ClassSchedule (CourseClassID, DayOfWeek, Shift, Room) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cs.getCourseClassID());
            pstmt.setString(2, cs.getDayOfWeek());
            pstmt.setString(3, cs.getShift());
            pstmt.setString(4, cs.getRoom());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 3. Cập nhật lịch học
    public boolean updateSchedule(ClassSchedule cs) {
        String sql = "UPDATE ClassSchedule SET CourseClassID=?, DayOfWeek=?, Shift=?, Room=? WHERE ScheduleID=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cs.getCourseClassID());
            pstmt.setString(2, cs.getDayOfWeek());
            pstmt.setString(3, cs.getShift());
            pstmt.setString(4, cs.getRoom());
            pstmt.setInt(5, cs.getScheduleID());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 4. Xóa lịch học
    public boolean deleteSchedule(int id) {
        String sql = "DELETE FROM ClassSchedule WHERE ScheduleID=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 5. Tìm kiếm
    public List<ClassSchedule> searchSchedule(String keyword) {
        List<ClassSchedule> list = new ArrayList<>();
        String sql = "SELECT * FROM ClassSchedule WHERE CourseClassID LIKE ? OR Room LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String k = "%" + keyword + "%";
            pstmt.setString(1, k);
            pstmt.setString(2, k);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new ClassSchedule(
                        rs.getInt("ScheduleID"),
                        rs.getString("CourseClassID"),
                        rs.getString("DayOfWeek"),
                        rs.getString("Shift"),
                        rs.getString("Room")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // 6. Lấy lịch học theo Lớp
    public List<ClassSchedule> getScheduleByClass(String courseClassID) {
        List<ClassSchedule> list = new ArrayList<>();
        String sql = "SELECT * FROM ClassSchedule WHERE CourseClassID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, courseClassID);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new ClassSchedule(
                        rs.getInt("ScheduleID"),
                        rs.getString("CourseClassID"),
                        rs.getString("DayOfWeek"),
                        rs.getString("Shift"),
                        rs.getString("Room")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // 7. Lấy lịch dạy theo Giảng viên (Giữ nguyên)
    public List<ClassSchedule> getSchedulesByProfessor(String professorID) {
        List<ClassSchedule> list = new ArrayList<>();
        String sql = "SELECT cs.* FROM ClassSchedule cs " +
                "JOIN CourseClass cc ON cs.CourseClassID = cc.CourseClassId " +
                "WHERE cc.ProfessorID = ? " +
                "ORDER BY FIELD(cs.DayOfWeek, 'Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7', 'CN'), cs.Shift";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, professorID);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new ClassSchedule(
                        rs.getInt("ScheduleID"),
                        rs.getString("CourseClassID"),
                        rs.getString("DayOfWeek"),
                        rs.getString("Shift"),
                        rs.getString("Room")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // 8. Lấy lịch dạy chi tiết theo Giảng viên (Phương thức MỚI để hỗ trợ lọc Tháng/Năm)
    /*
     * Phương thức này trả về List<Object[]> với các trường sau (theo thứ tự):
     * [0] - Integer: ScheduleID
     * [1] - String: CourseClassID
     * [2] - String: DayOfWeek
     * [3] - String: Shift
     * [4] - String: Room
     * [5] - String: SemesterID
     * [6] - Date: StartDate (của Semester)
     * [7] - Date: EndDate (của Semester)
     */
    public List<Object[]> getDetailedSchedulesByProfessor(String professorID) {
        List<Object[]> list = new ArrayList<>();
        // Join với CourseClass và Semester để lấy đầy đủ thông tin thời gian học kỳ
        String sql = "SELECT cs.ScheduleID, cs.CourseClassID, cs.DayOfWeek, cs.Shift, cs.Room, " +
                "cc.SemesterID, s.StartDate, s.EndDate " +
                "FROM ClassSchedule cs " +
                "JOIN CourseClass cc ON cs.CourseClassID = cc.CourseClassId " +
                "JOIN Semester s ON cc.SemesterID = s.SemesterID " +
                "WHERE cc.ProfessorID = ? " +
                "ORDER BY s.StartDate, FIELD(cs.DayOfWeek, 'Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7', 'CN'), cs.Shift";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, professorID);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Object[] {
                        rs.getInt("ScheduleID"),
                        rs.getString("CourseClassID"),
                        rs.getString("DayOfWeek"),
                        rs.getString("Shift"),
                        rs.getString("Room"),
                        rs.getString("SemesterID"),
                        rs.getDate("StartDate"), // java.sql.Date
                        rs.getDate("EndDate")    // java.sql.Date
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}