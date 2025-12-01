package com.db_quanlydiem.model;

public class CourseClass {
    private String courseClassId;
    private String subjectId;
    private String className;
    private int subjectCredits;
    private String semesterID;
    private String professorID;
    private int courseCapacity;

    public CourseClass(String courseClassId, String subjectId, String className, int subjectCredits, String semesterID, String professorID, int courseCapacity) {
        this.courseClassId = courseClassId;
        this.subjectId = subjectId;
        this.className = className;
        this.subjectCredits = subjectCredits;
        this.semesterID = semesterID;
        this.professorID = professorID;
        this.courseCapacity = courseCapacity;
    }

    public String getCourseClassId() { return courseClassId; }
    public void setCourseClassId(String courseClassId) { this.courseClassId = courseClassId; }

    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public int getSubjectCredits() { return subjectCredits; }
    public void setSubjectCredits(int subjectCredits) { this.subjectCredits = subjectCredits; }

    public String getSemesterID() { return semesterID; }
    public void setSemesterID(String semesterID) { this.semesterID = semesterID; }

    public String getProfessorID() { return professorID; }
    public void setProfessorID(String professorID) { this.professorID = professorID; }

    public int getCourseCapacity() { return courseCapacity; }
    public void setCourseCapacity(int courseCapacity) { this.courseCapacity = courseCapacity; }

    @Override
    public String toString() {
        return courseClassId + " - " + className; // Hiển thị mã lớp và tên lớp
    }
}