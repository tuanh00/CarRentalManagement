// In CarRentalApp.java
package com.example.carrentalapp.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.carrentalapp.R;
import com.example.carrentalapp.utilities.LoginActivity;
import com.example.carrentalapp.utilities.UpdateContractStatusWorker;
import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.TimeUnit;

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

        PeriodicWorkRequest contractStatusUpdateRequest =
                new PeriodicWorkRequest.Builder(UpdateContractStatusWorker.class, 12, TimeUnit.HOURS)
                        .build();

        WorkManager.getInstance(getApplicationContext()).enqueue(contractStatusUpdateRequest);


    }
}
