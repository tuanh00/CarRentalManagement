// Contract.java
package com.example.carrentalapp.models;

import com.example.carrentalapp.states.contract.ContractState;
import com.google.firebase.Timestamp;

public class Contract {
    private String id;
    private String userId;
    private String carId;
    private Timestamp createdAt;
    private Timestamp startDate;
    private Timestamp endDate;
    private double totalPayment;
    private ContractState state;
    private String eventId;

    public Contract() {
        this.state = ContractState.ACTIVE; // Default state
    }

    // Getters and Setters

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }

    public void setUserId(String userId) { this.userId = userId; }

    public String getCarId() { return carId; }

    public void setCarId(String carId) { this.carId = carId; }

    public Timestamp getCreatedAt() { return createdAt; }

    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getStartDate() { return startDate; }

    public void setStartDate(Timestamp startDate) { this.startDate = startDate; }

    public Timestamp getEndDate() { return endDate; }

    public void setEndDate(Timestamp endDate) { this.endDate = endDate; }

    public double getTotalPayment() { return totalPayment; }

    public void setTotalPayment(double totalPayment) { this.totalPayment = totalPayment; }

    public ContractState getState() { return state; }

    public void setState(ContractState state) { this.state = state; }

    public String getEventId() { return eventId; }

    public void setEventId(String eventId) { this.eventId = eventId; }
}
