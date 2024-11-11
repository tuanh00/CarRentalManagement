// UserEngineer.java
package com.example.carrentalapp.builders;

import com.example.carrentalapp.models.User;

public class UserEngineer {
    private IUserBuilder userBuilder;

    public UserEngineer(IUserBuilder userBuilder) {
        this.userBuilder = userBuilder;
    }

    public void constructUser() {
        // Fields should be set before calling this method
        userBuilder.setRole();
    }

    public User getUser() {
        return userBuilder.build();
    }
}
