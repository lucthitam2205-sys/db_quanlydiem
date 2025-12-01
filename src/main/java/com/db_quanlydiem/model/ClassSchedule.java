package com.db_quanlydiem.model;

public class ClassSchedule {
    private int scheduleID;
    private String courseClassID;
    private String dayOfWeek;
    private String shift;
    private String room;

    public ClassSchedule(int scheduleID, String courseClassID, String dayOfWeek, String shift, String room) {
        this.scheduleID = scheduleID;
        this.courseClassID = courseClassID;
        this.dayOfWeek = dayOfWeek;
        this.shift = shift;
        this.room = room;
    }

    public int getScheduleID() { return scheduleID; }
    public void setScheduleID(int scheduleID) { this.scheduleID = scheduleID; }

    public String getCourseClassID() { return courseClassID; }
    public void setCourseClassID(String courseClassID) { this.courseClassID = courseClassID; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    @Override
    public String toString() {
        return dayOfWeek + " - " + shift + " (" + room + ")"; // Hiển thị thông tin lịch học tóm tắt
    }
}