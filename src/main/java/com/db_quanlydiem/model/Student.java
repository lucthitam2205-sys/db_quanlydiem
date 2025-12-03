package com.db_quanlydiem.model;

import java.sql.Date;

public class Student {
    private String studentID;
    private String studentName;
    private Date studentDOB;
    private String studentGender;
    private String studentCohort;
    private String studentMajor;
    private String studentEmail;
    private String studentPhone;
    private String studentHometown;
    private String parentName;
    private String parentPhone;
    private String studentStatus;

    // Constructor
    public Student(String studentID, String studentName, Date studentDOB, String studentGender,
                   String studentCohort,
                   String studentMajor, String studentEmail, String studentPhone,
                   String studentHometown, String parentName, String parentPhone,
                   String studentStatus) {
        this.studentID = studentID;
        this.studentName = studentName;
        this.studentDOB = studentDOB;
        this.studentGender = studentGender;
        this.studentCohort = studentCohort;
        this.studentMajor = studentMajor;
        this.studentEmail = studentEmail;
        this.studentPhone = studentPhone;
        this.studentHometown = studentHometown;
        this.parentName = parentName;
        this.parentPhone = parentPhone;
        this.studentStatus = studentStatus;
    }

    // Getter & Setter
    public String getStudentCohort() { return studentCohort; }
    public void setStudentCohort(String studentCohort) { this.studentCohort = studentCohort; }

    // ... Các Getter/Setter khác giữ nguyên ...
    public String getStudentID() { return studentID; }
    public void setStudentID(String studentID) { this.studentID = studentID; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public Date getStudentDOB() { return studentDOB; }
    public void setStudentDOB(Date studentDOB) { this.studentDOB = studentDOB; }
    public String getStudentGender() { return studentGender; }
    public void setStudentGender(String studentGender) { this.studentGender = studentGender; }
    public String getStudentMajor() { return studentMajor; }
    public void setStudentMajor(String studentMajor) { this.studentMajor = studentMajor; }
    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }
    public String getStudentPhone() { return studentPhone; }
    public void setStudentPhone(String studentPhone) { this.studentPhone = studentPhone; }
    public String getStudentHometown() { return studentHometown; }
    public void setStudentHometown(String studentHometown) { this.studentHometown = studentHometown; }
    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }
    public String getParentPhone() { return parentPhone; }
    public void setParentPhone(String parentPhone) { this.parentPhone = parentPhone; }
    public String getStudentStatus() { return studentStatus; }
    public void setStudentStatus(String studentStatus) { this.studentStatus = studentStatus; }

    @Override
    public String toString() {
        return studentName + " - " + studentID;
    }
}