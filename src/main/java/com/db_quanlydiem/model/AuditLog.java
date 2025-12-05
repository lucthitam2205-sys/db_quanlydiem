package com.db_quanlydiem.model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class AuditLog {
    private int logID;
    private String username;
    private String actionType;
    private String description;
    private Timestamp logTime;

    // Constructor đầy đủ (Dùng khi load từ DB)
    public AuditLog(int logID, String username, String actionType, String description, Timestamp logTime) {
        this.logID = logID;
        this.username = username;
        this.actionType = actionType;
        this.description = description;
        this.logTime = logTime;
    }

    // Constructor rút gọn (Dùng khi thêm mới)
    public AuditLog(String username, String actionType, String description) {
        this.username = username;
        this.actionType = actionType;
        this.description = description;
    }

    public int getLogID() { return logID; }
    public String getUsername() { return username; }
    public String getActionType() { return actionType; }
    public String getDescription() { return description; }
    public Timestamp getLogTime() { return logTime; }

    // Helper để hiển thị giờ đẹp trên bảng
    public String getFormattedTime() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(logTime);
    }
}