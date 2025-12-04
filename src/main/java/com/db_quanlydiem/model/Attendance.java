package com.db_quanlydiem.model;

import java.sql.Date;

public class Attendance {
    private int attendanceID;
    private String studentID;
    private String courseClassID;
    private Date attendanceDate;
    private boolean isPresent;
    private String note;

    public Attendance(int attendanceID, String studentID, String courseClassID, Date attendanceDate, boolean isPresent, String note) {
        this.attendanceID = attendanceID;
        this.studentID = studentID;
        this.courseClassID = courseClassID;
        this.attendanceDate = attendanceDate;
        this.isPresent = isPresent;
        this.note = note;
    }

    // Constructor cho việc thêm/cập nhật (không cần ID tự tăng)
    public Attendance(String studentID, String courseClassID, Date attendanceDate, boolean isPresent, String note) {
        this(0, studentID, courseClassID, attendanceDate, isPresent, note);
    }

    // Getters
    public int getAttendanceID() { return attendanceID; }
    public String getStudentID() { return studentID; }
    public String getCourseClassID() { return courseClassID; }
    public Date getAttendanceDate() { return attendanceDate; }
    public boolean isPresent() { return isPresent; }
    public String getNote() { return note; }

    // Setters (Nếu cần)
    public void setAttendanceID(int attendanceID) { this.attendanceID = attendanceID; }
    public void setPresent(boolean present) { isPresent = present; }
    public void setNote(String note) { this.note = note; }
}