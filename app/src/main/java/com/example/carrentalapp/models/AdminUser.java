package com.example.carrentalapp.models;

public class AdminUser extends User {
    public AdminUser() {}

    public AdminUser(String uid, String firstName, String lastName, String email, String phoneNumber) {
        super(uid, firstName, lastName, email, phoneNumber, "admin", System.currentTimeMillis());
    }

    // Admin-specific methods can be added here
}