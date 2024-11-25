package com.example.carrentalapp.builders;

import com.example.carrentalapp.models.User;
import com.google.firebase.Timestamp;

public interface IUserBuilder {
    void setUid(String uid);
    void setFirstName(String firstName);
    void setLastName(String lastName);
    void setEmail(String email);
    void setPhoneNumber(String phoneNumber);
    void setRole();
    void setImgUrl(String imgUrl);
    void setBlocked(boolean blocked);
    void setCreatedAt(Timestamp createdAt);
    User build();

}
