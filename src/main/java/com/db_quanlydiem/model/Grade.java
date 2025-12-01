package com.db_quanlydiem.model;

public class Grade {
    private String studentID;
    private String courseClassID;
    private String semesterID;
    private double gradeAssessment1;
    private double gradeAssessment2;
    private double gradeFinal;
    private double gradeAverage;
    private String gradeNote;

    public Grade(String studentID, String courseClassID, String semesterID, double gradeAssessment1, double gradeAssessment2, double gradeFinal, double gradeAverage, String gradeNote) {
        this.studentID = studentID;
        this.courseClassID = courseClassID;
        this.semesterID = semesterID;
        this.gradeAssessment1 = gradeAssessment1;
        this.gradeAssessment2 = gradeAssessment2;
        this.gradeFinal = gradeFinal;
        this.gradeAverage = gradeAverage;
        this.gradeNote = gradeNote;
    }

    public String getStudentID() { return studentID; }
    public void setStudentID(String studentID) { this.studentID = studentID; }

    public String getCourseClassID() { return courseClassID; }
    public void setCourseClassID(String courseClassID) { this.courseClassID = courseClassID; }

    public String getSemesterID() { return semesterID; }
    public void setSemesterID(String semesterID) { this.semesterID = semesterID; }

    public double getGradeAssessment1() { return gradeAssessment1; }
    public void setGradeAssessment1(double gradeAssessment1) { this.gradeAssessment1 = gradeAssessment1; }

    public double getGradeAssessment2() { return gradeAssessment2; }
    public void setGradeAssessment2(double gradeAssessment2) { this.gradeAssessment2 = gradeAssessment2; }

    public double getGradeFinal() { return gradeFinal; }
    public void setGradeFinal(double gradeFinal) { this.gradeFinal = gradeFinal; }

    public double getGradeAverage() { return gradeAverage; }
    public void setGradeAverage(double gradeAverage) { this.gradeAverage = gradeAverage; }

    public String getGradeNote() { return gradeNote; }
    public void setGradeNote(String gradeNote) { this.gradeNote = gradeNote; }

    @Override
    public String toString() {
        return "Điểm TB: " + gradeAverage; // Hiển thị điểm trung bình
    }
}