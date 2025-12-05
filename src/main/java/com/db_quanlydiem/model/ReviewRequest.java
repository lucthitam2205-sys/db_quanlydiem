package com.db_quanlydiem.model;

import java.sql.Timestamp;

public class ReviewRequest {
    private int requestID;
    private String studentID;
    private String courseClassID;
    private String reason;
    private String status;
    private String response;
    private Timestamp requestDate;

    // Constructor 1: Đầy đủ tham số (Dùng khi lấy dữ liệu từ DB lên)
    public ReviewRequest(int requestID, String studentID, String courseClassID, String reason, String status, String response, Timestamp requestDate) {
        this.requestID = requestID;
        this.studentID = studentID;
        this.courseClassID = courseClassID;
        this.reason = reason;
        this.status = status;
        this.response = response;
        this.requestDate = requestDate;
    }

    // Constructor 2: Rút gọn (Dùng để tạo yêu cầu mới từ giao diện)
    // Đây là constructor mà Controller đang cố gọi
    public ReviewRequest(String studentID, String courseClassID, String reason) {
        this.studentID = studentID;
        this.courseClassID = courseClassID;
        this.reason = reason;
        // Các trường khác sẽ để null hoặc do Database tự sinh (Auto Increment, Default)
    }

    // Getters & Setters
    public int getRequestID() { return requestID; }
    public void setRequestID(int requestID) { this.requestID = requestID; }

    public String getStudentID() { return studentID; }
    public void setStudentID(String studentID) { this.studentID = studentID; }

    public String getCourseClassID() { return courseClassID; }
    public void setCourseClassID(String courseClassID) { this.courseClassID = courseClassID; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public Timestamp getRequestDate() { return requestDate; }
    public void setRequestDate(Timestamp requestDate) { this.requestDate = requestDate; }
}