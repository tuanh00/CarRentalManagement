// AdminBuilder.java
package com.example.carrentalapp.builders;

import com.example.carrentalapp.models.AdminUser;
import com.example.carrentalapp.models.User;

public class AdminBuilder implements IUserBuilder {
    private String uid;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

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
    public User build() {
        return new AdminUser(uid, firstName, lastName, email, phoneNumber);
    }
}