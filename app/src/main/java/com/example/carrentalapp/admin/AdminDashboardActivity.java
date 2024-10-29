package com.example.carrentalapp.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.carrentalapp.R;
import com.example.carrentalapp.common.AddCarFragment;
import com.example.carrentalapp.common.ViewCarsFragment;


public class AdminDashboardActivity extends AppCompatActivity {

    private Button buttonAddCar, buttonViewCars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        buttonAddCar = findViewById(R.id.buttonAddCar);
        buttonViewCars = findViewById(R.id.buttonViewCars);

        // Set default fragment to AddCarFragment
        replaceFragment(new AddCarFragment());

        // Set button click listeners to replace fragments
        buttonAddCar.setOnClickListener(v -> replaceFragment(new AddCarFragment()));
        buttonViewCars.setOnClickListener(v -> replaceFragment(new ViewCarsFragment()));
    }
<<<<<<< HEAD
=======

    // Function to replace fragments
    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
>>>>>>> 3a8824d (Build admindashboard functions: Add New Car and Display All Cars)
}
