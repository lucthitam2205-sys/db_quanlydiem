package com.db_quanlydiem.model;

import java.sql.Date; // Hoặc dùng java.time.LocalDate tùy sở thích, nhưng Date dễ map với JDBC cũ hơn

public class Attendance {
    private int attendanceID;
    private String studentID;
    private String courseClassID;
    private Date attendanceDate;
    private boolean isPresent;
    private String attendanceNote;

    // Constructor đầy đủ
    public Attendance(int attendanceID, String studentID, String courseClassID, Date attendanceDate, boolean isPresent, String attendanceNote) {
        this.attendanceID = attendanceID;
        this.studentID = studentID;
        this.courseClassID = courseClassID;
        this.attendanceDate = attendanceDate;
        this.isPresent = isPresent;
        this.attendanceNote = attendanceNote;
    }

    // Constructor rút gọn (để thêm mới - không cần ID)
    public Attendance(String studentID, String courseClassID, Date attendanceDate, boolean isPresent, String attendanceNote) {
        this.studentID = studentID;
        this.courseClassID = courseClassID;
        this.attendanceDate = attendanceDate;
        this.isPresent = isPresent;
        this.attendanceNote = attendanceNote;
    }

    // Getters và Setters
    public int getAttendanceID() { return attendanceID; }
    public void setAttendanceID(int attendanceID) { this.attendanceID = attendanceID; }

    public String getStudentID() { return studentID; }
    public void setStudentID(String studentID) { this.studentID = studentID; }

    public String getCourseClassID() { return courseClassID; }
    public void setCourseClassID(String courseClassID) { this.courseClassID = courseClassID; }

    public Date getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(Date attendanceDate) { this.attendanceDate = attendanceDate; }

    public boolean isPresent() { return isPresent; }
    public void setPresent(boolean present) { isPresent = present; }

    public String getAttendanceNote() { return attendanceNote; }
    public void setAttendanceNote(String attendanceNote) { this.attendanceNote = attendanceNote; }
}