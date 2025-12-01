package com.db_quanlydiem.model;

import java.sql.Timestamp;

public class Account {
    private String username;
    private String password;
    private String roleName;
    private Timestamp createdDate;

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