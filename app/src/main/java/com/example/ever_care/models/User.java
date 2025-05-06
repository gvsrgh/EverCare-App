package com.example.ever_care.models;

public class User {
    private String id;
    private String fullName;
    private String email;
    private int age;
    private String gender;
    private boolean isElderly;
    private String linkedUserId; // For family members to link to elderly
    private String phoneNumber; // Added phone number field

    public User() {
        // Required empty constructor for Firebase
    }

    public User(String id, String fullName, String email, int age, String gender, boolean isElderly) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.age = age;
        this.gender = gender;
        this.isElderly = isElderly;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public boolean isElderly() {
        return isElderly;
    }

    public void setElderly(boolean elderly) {
        isElderly = elderly;
    }

    public String getLinkedUserId() {
        return linkedUserId;
    }

    public void setLinkedUserId(String linkedUserId) {
        this.linkedUserId = linkedUserId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}