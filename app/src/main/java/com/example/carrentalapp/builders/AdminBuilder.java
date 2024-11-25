// AdminBuilder.java
package com.example.carrentalapp.builders;

import com.example.carrentalapp.models.AdminUser;
import com.example.carrentalapp.models.User;
import com.google.firebase.Timestamp;

public class AdminBuilder implements IUserBuilder {
    private String uid;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String imgUrl;
    private boolean blocked;
    private Timestamp createdAt;

    @Override
    public void setUid(String uid) { this.uid = uid; }
    @Override
    public void setFirstName(String firstName) { this.firstName = firstName; }
    @Override
    public void setLastName(String lastName) { this.lastName = lastName; }
    @Override
    public void setEmail(String email) { this.email = email; }
    @Override
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    @Override
    public void setRole() { /* Role is set in AdminUser constructor */ }
    @Override
    public void setImgUrl(String imgUrl) { this.imgUrl = imgUrl; }
    @Override
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    @Override
    public User build() {
        return new AdminUser(uid, firstName, lastName, email, phoneNumber, imgUrl, blocked, createdAt);
    }
}
