// Car.java
package com.example.carrentalapp.models;

import com.example.carrentalapp.states.car.CarAvailabilityState;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;

public class Car {
    private String id;
    private String model;
    private String brand;
    private int seats;
    private String location;
    private ArrayList<String> images;
    private double price;
    private float rating;
    private int ratingCount;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    @PropertyName("state")
    private CarAvailabilityState currentState;

    // Deprecated fields
    @Deprecated
    private double latitude;
    @Deprecated
    private double longitude;


    // Default constructor (required by Firebase)
    public Car() {
        this.images = new ArrayList<>();
    }

    // Custom constructor for creating a new car
    public Car(String model, String brand, int seats, String location, double price) {
        this.model = model;
        this.brand = brand;
        this.seats = seats;
        this.location = location;
        this.price = price;
        this.images = new ArrayList<>();
        this.currentState = CarAvailabilityState.AVAILABLE; // Default to AVAILABLE when creating a new car
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public ArrayList<String> getImages() { return images; }
    public void setImages(ArrayList<String> images) { this.images = images; }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    @PropertyName("state")
    public CarAvailabilityState getCurrentState() {
        return currentState;
    }

    @PropertyName("state")
    public void setCurrentState(CarAvailabilityState currentState) {
        this.currentState = currentState;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Deprecated getters and setters
    @Deprecated
    public double getLatitude() { return latitude; }
    @Deprecated
    public void setLatitude(double latitude) { this.latitude = latitude; }

    @Deprecated
    public double getLongitude() { return longitude; }
    @Deprecated
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
