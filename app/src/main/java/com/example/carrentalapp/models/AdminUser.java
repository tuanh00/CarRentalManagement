package com.example.carrentalapp.models;

import com.google.firebase.Timestamp;

import java.sql.Time;

public class AdminUser extends User {
    public AdminUser() {}

    public AdminUser(String uid, String firstName, String lastName, String email, String phoneNumber, String imgUrl, boolean blocked, Timestamp createdAt) {
        super(uid, firstName, lastName, email, phoneNumber, "admin", createdAt, imgUrl, blocked);
    }

    // Admin-specific methods can be added here
}