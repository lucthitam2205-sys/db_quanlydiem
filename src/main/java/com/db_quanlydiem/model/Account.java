package com.db_quanlydiem.model;

import java.sql.Timestamp;

public class Account {
    private String username;  // Tên đăng nhập hệ thống
    private String password;  // Mật khẩu đăng nhập
    private String roleName;  // Tên vai trò/Quyền hạn
    private Timestamp createdDate;  // Ngày giờ tạo tài khoản

    public Account(String username, String password, String roleName, Timestamp createdDate) {
        this.username = username;
        this.password = password;
        this.roleName = roleName;
        this.createdDate = createdDate;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public Timestamp getCreatedDate() { return createdDate; }
    public void setCreatedDate(Timestamp createdDate) { this.createdDate = createdDate; }
}