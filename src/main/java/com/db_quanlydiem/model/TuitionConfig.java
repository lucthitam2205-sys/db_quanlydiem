package com.db_quanlydiem.model;

public class TuitionConfig {
    private int configID;
    private String semesterID;
    private double pricePerCredit;
    private String description;

    public TuitionConfig(int configID, String semesterID, double pricePerCredit, String description) {
        this.configID = configID;
        this.semesterID = semesterID;
        this.pricePerCredit = pricePerCredit;
        this.description = description;
    }

    public int getConfigID() { return configID; }
    public void setConfigID(int configID) { this.configID = configID; }

    public String getSemesterID() { return semesterID; }
    public void setSemesterID(String semesterID) { this.semesterID = semesterID; }

    public double getPricePerCredit() { return pricePerCredit; }
    public void setPricePerCredit(double pricePerCredit) { this.pricePerCredit = pricePerCredit; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}