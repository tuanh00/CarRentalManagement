package com.example.carrentalapp.builders;

import com.example.carrentalapp.models.User;

public interface IUserBuilder {
    void setUid(String uid);
    void setFirstName(String firstName);
    void setLastName(String lastName);
    void setEmail(String email);
    void setPhoneNumber(String phoneNumber);
    void setRole();
    User build();

}
