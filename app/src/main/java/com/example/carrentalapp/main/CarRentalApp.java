// In CarRentalApp.java
package com.example.carrentalapp.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.carrentalapp.R;
import com.example.carrentalapp.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class CarRentalApp extends AppCompatActivity {

    private Button signOutButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_rental_app);

        mAuth = FirebaseAuth.getInstance();
        signOutButton = findViewById(R.id.signOutButton);

        signOutButton.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(CarRentalApp.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
