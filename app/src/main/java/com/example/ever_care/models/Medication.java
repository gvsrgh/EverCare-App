package com.example.ever_care.models;

import java.util.Date;

public class Medication {
    private String id;
    private String name;
    private String dosage;
    private String frequency;
    private Date startDate;
    private Date endDate;
    private String reminderTime;
    private boolean taken;
    private Date lastTakenTime;
    private String elderlyId;
    private String createdBy;

    public Medication() {
        // Required empty constructor for Firebase
    }

    public Medication(String id, String name, String dosage, String frequency,
                      Date startDate, Date endDate, String reminderTime,
                      String elderlyId, String createdBy) {
        this.id = id;
        this.name = name;
        this.dosage = dosage;
        this.frequency = frequency;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reminderTime = reminderTime;
        this.taken = false;
        this.elderlyId = elderlyId;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public boolean isTaken() {
        return taken;
    }

    public void setTaken(boolean taken) {
        this.taken = taken;
    }

    public Date getLastTakenTime() {
        return lastTakenTime;
    }

    public void setLastTakenTime(Date lastTakenTime) {
        this.lastTakenTime = lastTakenTime;
    }

    public String getElderlyId() {
        return elderlyId;
    }

    public void setElderlyId(String elderlyId) {
        this.elderlyId = elderlyId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}