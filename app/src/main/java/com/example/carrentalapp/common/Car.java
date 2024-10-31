package com.example.carrentalapp.common;

import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;
import java.util.List;

public class Car {
    private String model;
    private String brand;
    private int seats;
    private String location;
    private List<String> imageUrls; // Changed to List of image URLs
    private double price;



    public Car() {  this.imageUrls = new ArrayList<>(); }

    public Car(String model, String brand, int seats, String location, List<String> imageUrls, double price) {

        this.model = model;
        this.brand = brand;
        this.seats = seats;
        this.location = location;
        this.imageUrls = imageUrls;
        this.price = price;
    }

    // Getters and Setters for each field
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @PropertyName("images")
    public List<String> getImageUrls() {
        return imageUrls;
    }

    @PropertyName("images")
    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
