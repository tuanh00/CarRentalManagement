package com.example.carrentalapp.models;

public class User {
    protected String uid;
    protected String firstName;
    protected String lastName;
    protected String email;
    protected String phoneNumber;
    protected String role;
    protected long createdAt; // Timestamp in milliseconds

    public User(){}

    public User(String uid, String firstName, String lastName, String email, String phoneNumber, String role, long createdAt) {
        this.uid = uid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.createdAt = createdAt;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getCreatedAt() { return createdAt; }

    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
