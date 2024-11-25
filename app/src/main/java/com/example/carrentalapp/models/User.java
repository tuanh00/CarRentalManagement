package com.example.carrentalapp.models;

import com.google.firebase.Timestamp;

public class User {
    protected String uid;
    protected String firstName;
    protected String lastName;
    protected String email;
    protected String phoneNumber;
    protected String role;
    protected Timestamp createdAt;
    protected String imgUrl;
    protected boolean blocked;

    public User(){}

    public User(String uid, String firstName, String lastName, String email, String phoneNumber, String role, Timestamp createdAt, String imgUrl, boolean blocked) {
        this.uid = uid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.createdAt = createdAt;
        this.imgUrl = imgUrl;
        this.blocked = blocked;
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

    public Timestamp getCreatedAt() { return createdAt; }

    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getImgUrl() {return imgUrl;}

    public void setImgUrl(String imgUrl) {this.imgUrl = imgUrl;}

    public boolean isBlocked() {return blocked;}

    public void setBlocked(boolean blocked) {this.blocked = blocked;}
}
