package com.db_quanlydiem.dao;

import com.db_quanlydiem.DatabaseConnection;
import com.db_quanlydiem.model.ReviewRequest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewRequestDAO {

    // 1. Gửi yêu cầu (Sinh viên dùng)
    public boolean addRequest(ReviewRequest req) {
        String sql = "INSERT INTO ReviewRequest (StudentID, CourseClassID, Reason, Status, RequestDate) VALUES (?, ?, ?, 'Pending', NOW())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, req.getStudentID());
            ps.setString(2, req.getCourseClassID());
            ps.setString(3, req.getReason());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2. Lấy danh sách yêu cầu của 1 lớp (Giảng viên dùng) -> ĐÂY LÀ HÀM QUAN TRỌNG CẦN SỬA
    public List<ReviewRequest> getRequestsByClass(String classID) {
        List<ReviewRequest> list = new ArrayList<>();
        // Query lấy dữ liệu từ bảng ReviewRequest
        String sql = "SELECT * FROM ReviewRequest WHERE CourseClassID = ? ORDER BY RequestDate DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, classID);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new ReviewRequest(
                        rs.getInt("RequestID"),
                        rs.getString("StudentID"),
                        rs.getString("CourseClassID"),
                        rs.getString("Reason"),
                        rs.getString("Status"),
                        rs.getString("Response"),
                        rs.getTimestamp("RequestDate")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}