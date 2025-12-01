package com.db_quanlydiem.model;

import java.sql.Date;

public class Semester {
    private String semesterID;
    private String semesterName;
    private Date startDate;
    private Date endDate;

    public Semester(String semesterID, String semesterName, Date startDate, Date endDate) {
        this.semesterID = semesterID;
        this.semesterName = semesterName;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getSemesterID() { return semesterID; }
    public void setSemesterID(String semesterID) { this.semesterID = semesterID; }

    public String getSemesterName() { return semesterName; }
    public void setSemesterName(String semesterName) { this.semesterName = semesterName; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    @Override
    public String toString() {
        return semesterName; // Hiển thị tên học kỳ trên giao diện (ComboBox)
    }
}