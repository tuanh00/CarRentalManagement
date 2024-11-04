package com.example.carrentalapp.admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.example.carrentalapp.R;
import com.example.carrentalapp.auth.LoginActivity;
import com.example.carrentalapp.auth.SignOutActivity;
import com.example.carrentalapp.common.AddCarFragment;
import com.example.carrentalapp.common.RentCarFragment;
import com.example.carrentalapp.common.ViewCarsFragment;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {
//Darshini's comment
    private Button buttonAddCar, buttonViewCars, btnSignout, buttonRentCar;
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        Toolbar toolbar = findViewById(R.id.adminToolbar);
        setSupportActionBar(toolbar);

        SharedPreferences sharedPreferences = getSharedPreferences("CarRentalAppPrefs", MODE_PRIVATE);
        String role = sharedPreferences.getString("user_role", "no role found");
        Log.d("AdminDashboardActivity", "User role in Admin Dashboard: " + role);


        buttonAddCar = findViewById(R.id.buttonAddCar);
        buttonViewCars = findViewById(R.id.buttonViewCars);
        btnSignout = findViewById(R.id.btnSignOut); // Initialize btnSignout
        //buttonRentCar = findViewById(R.id.buttonRentCar); // Rent Car button

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Google Sign-In client
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this, gso);

        // Set default fragment to AddCarFragment
        replaceFragment(new AddCarFragment());

        // Set button click listeners to replace fragments
        buttonAddCar.setOnClickListener(v -> replaceFragment(new AddCarFragment()));
        buttonViewCars.setOnClickListener(v -> replaceFragment(new ViewCarsFragment()));
        //buttonRentCar.setOnClickListener(v -> replaceFragment(new RentCarFragment()));

        //Set sign-out button event
        btnSignout.setOnClickListener(v -> SignOutActivity.signOut(this, mAuth, googleSignInClient));
    }

    // Function to replace fragments
    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.adminFragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

//    private void signOut() {
//        // Firebase sign out
//        mAuth.signOut();
//
//        // Google sign out
//        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
//            // Navigate back to login screen
//            Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
//            // Clear activity stack to prevent back navigation
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(intent);
//            finish();
//        });
//    }
}
