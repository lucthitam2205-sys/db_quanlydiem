package com.db_quanlydiem.model;

public class Professor {
    private String professorID;
    private String professorName;
    private String professorEmail;
    private String professorPhone;
    private String professorHometown;
    private String professorTitle;

    public Professor(String professorID, String professorName, String professorEmail, String professorPhone, String professorHometown, String professorTitle) {
        this.professorID = professorID;
        this.professorName = professorName;
        this.professorEmail = professorEmail;
        this.professorPhone = professorPhone;
        this.professorHometown = professorHometown;
        this.professorTitle = professorTitle;
    }

    public String getProfessorID() { return professorID; }
    public void setProfessorID(String professorID) { this.professorID = professorID; }

    public String getProfessorName() { return professorName; }
    public void setProfessorName(String professorName) { this.professorName = professorName; }

    public String getProfessorEmail() { return professorEmail; }
    public void setProfessorEmail(String professorEmail) { this.professorEmail = professorEmail; }

    public String getProfessorPhone() { return professorPhone; }
    public void setProfessorPhone(String professorPhone) { this.professorPhone = professorPhone; }

    public String getProfessorHometown() { return professorHometown; }
    public void setProfessorHometown(String professorHometown) { this.professorHometown = professorHometown; }

    public String getProfessorTitle() { return professorTitle; }
    public void setProfessorTitle(String professorTitle) { this.professorTitle = professorTitle; }

    @Override
    public String toString() {
        return professorName; // Hiển thị tên giảng viên trên giao diện
    }
}