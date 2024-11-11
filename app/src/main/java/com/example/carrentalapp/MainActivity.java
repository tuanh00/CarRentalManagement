package com.example.carrentalapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.carrentalapp.utilities.LoginActivity;
import com.example.carrentalapp.main.CarRentalApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // User is logged in, redirect to the main app screen
            Intent intent = new Intent(MainActivity.this, CarRentalApp.class);
            startActivity(intent);
        } else {
            // No user logged in, redirect to the login screen
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        // Close MainActivity so it doesn't remain in the back stack
        finish();
    }
}
