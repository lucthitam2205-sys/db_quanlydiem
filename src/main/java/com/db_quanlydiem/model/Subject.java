package com.db_quanlydiem.model;

public class Subject {
    private String subjectId;
    private String subjectName;
    private int subjectCredit;

    public Subject(String subjectId, String subjectName, int subjectCredit) {
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.subjectCredit = subjectCredit;
    }

    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public int getSubjectCredit() { return subjectCredit; }
    public void setSubjectCredit(int subjectCredit) { this.subjectCredit = subjectCredit; }

    @Override
    public String toString() {
        return subjectName + " (" + subjectId + ")"; // Hiển thị tên môn kèm mã
    }
}