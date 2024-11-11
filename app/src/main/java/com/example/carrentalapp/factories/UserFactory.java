package com.example.carrentalapp.factories;

import com.example.carrentalapp.builders.AdminBuilder;
import com.example.carrentalapp.builders.CustomerBuilder;
import com.example.carrentalapp.builders.IUserBuilder;
import com.example.carrentalapp.builders.UserEngineer;
import com.example.carrentalapp.models.User;

public class UserFactory {
    public static User createUser(String role, String uid, String firstName, String lastName, String email, String phoneNumber, String driverLicenseId) {
        IUserBuilder userBuilder;

        if("admin".equalsIgnoreCase(role)) {
            userBuilder = new AdminBuilder();
        } else {
            userBuilder = new CustomerBuilder();
            ((CustomerBuilder) userBuilder).setDriverLicenseId(driverLicenseId);
        }

        userBuilder.setUid(uid);
        userBuilder.setFirstName(firstName);
        userBuilder.setLastName(lastName);
        userBuilder.setEmail(email);
        userBuilder.setPhoneNumber(phoneNumber);
        userBuilder.setRole();

        UserEngineer userEngineer = new UserEngineer(userBuilder);
        userEngineer.constructUser();

        return userEngineer.getUser();
    }
}
