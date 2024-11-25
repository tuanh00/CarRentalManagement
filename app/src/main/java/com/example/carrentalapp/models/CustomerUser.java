package com.example.carrentalapp.models;

public class CustomerUser extends User{
    private String driverLicenseId;
    private  int points;
    public CustomerUser(){}
    public CustomerUser(String uid, String firstName, String lastName, String email, String phoneNumber, String driverLicenseId, int points) {
        super(uid, firstName, lastName, email, phoneNumber, "customer", System.currentTimeMillis());
        this.driverLicenseId = driverLicenseId;
        this.points = points;
    }

    public String getDriverLicenseId() {
        return driverLicenseId;
    }

    public void setDriverLicenseId(String driverLicenseId) {
        this.driverLicenseId = driverLicenseId;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}
