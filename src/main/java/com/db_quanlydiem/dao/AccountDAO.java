package com.db_quanlydiem.dao;

import com.db_quanlydiem.DatabaseConnection;
import com.db_quanlydiem.model.Account;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {

    // 1. Kiểm tra đăng nhập (Giữ nguyên)
    public String checkLogin(String username, String password) {
        String sql = "SELECT RoleName FROM Account WHERE Username = ? AND Password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getString("RoleName");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // 2. Lấy danh sách tất cả tài khoản
    public List<Account> getAllAccounts() {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM Account ORDER BY CreatedDate DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Account(
                        rs.getString("Username"),
                        rs.getString("Password"),
                        rs.getString("RoleName"),
                        rs.getTimestamp("CreatedDate")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // 3. Thêm tài khoản mới
    public boolean addAccount(Account a) {
        String sql = "INSERT INTO Account (Username, Password, RoleName, CreatedDate) VALUES (?, ?, ?, NOW())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, a.getUsername());
            pstmt.setString(2, a.getPassword());
            pstmt.setString(3, a.getRoleName());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 4. Cập nhật tài khoản (Đổi mật khẩu & Quyền)
    public boolean updateAccount(Account a) {
        String sql = "UPDATE Account SET Password = ?, RoleName = ? WHERE Username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, a.getPassword());
            pstmt.setString(2, a.getRoleName());
            pstmt.setString(3, a.getUsername());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 5. Xóa tài khoản
    public boolean deleteAccount(String username) {
        String sql = "DELETE FROM Account WHERE Username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // 6. Tìm kiếm tài khoản
    public List<Account> searchAccount(String keyword) {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM Account WHERE Username LIKE ? OR RoleName LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String k = "%" + keyword + "%";
            pstmt.setString(1, k);
            pstmt.setString(2, k);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Account(
                        rs.getString("Username"),
                        rs.getString("Password"),
                        rs.getString("RoleName"),
                        rs.getTimestamp("CreatedDate")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // 7. Kiểm tra tồn tại
    public boolean isUsernameExists(String username) {
        String sql = "SELECT 1 FROM Account WHERE Username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}