package com.example.ever_care.models;

import java.util.Date;

public class HealthData {
    private String id;
    private float temperature;
    private String bloodPressure;
    private float sugarLevel;
    private Date recordedDate;
    private String elderlyId;

    public HealthData() {
        // Required empty constructor for Firebase
    }

    public HealthData(String id, float temperature, String bloodPressure,
                      float sugarLevel, Date recordedDate, String elderlyId) {
        this.id = id;
        this.temperature = temperature;
        this.bloodPressure = bloodPressure;
        this.sugarLevel = sugarLevel;
        this.recordedDate = recordedDate;
        this.elderlyId = elderlyId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public String getBloodPressure() {
        return bloodPressure;
    }

    public void setBloodPressure(String bloodPressure) {
        this.bloodPressure = bloodPressure;
    }

    public float getSugarLevel() {
        return sugarLevel;
    }

    public void setSugarLevel(float sugarLevel) {
        this.sugarLevel = sugarLevel;
    }

    public Date getRecordedDate() {
        return recordedDate;
    }

    public void setRecordedDate(Date recordedDate) {
        this.recordedDate = recordedDate;
    }

    public String getElderlyId() {
        return elderlyId;
    }

    public void setElderlyId(String elderlyId) {
        this.elderlyId = elderlyId;
    }
}