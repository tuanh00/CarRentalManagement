package com.example.carrentalapp.uiactivities.admin;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carrentalapp.BuildConfig;
import com.example.carrentalapp.R;
import com.example.carrentalapp.common.ContractAdapter;
import com.example.carrentalapp.common.ViewContractsFragment;
import com.example.carrentalapp.utilities.SignOutActivity;
import com.example.carrentalapp.common.ViewCarsFragment;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class AdminDashboardActivity extends AppCompatActivity {

    private Button buttonAddCar, buttonViewCars, btnSignout, buttonViewContracts;
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private static final String PREFS_NAME = "CarRentalAppPrefs";
    private static final String ROLE_KEY = "user_role";
    private RecyclerView recyclerViewContracts;
    private ContractAdapter contractAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.adminToolbar);
        setSupportActionBar(toolbar);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String role = sharedPreferences.getString(ROLE_KEY, "customer");
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (userId != null) {
            contractAdapter = new ContractAdapter(this, new ArrayList<>());
            contractAdapter.loadContractsBasedOnRole(role, userId);
        }

        buttonAddCar = findViewById(R.id.buttonAddCar);
        buttonViewCars = findViewById(R.id.buttonViewCars);
        btnSignout = findViewById(R.id.btnSignOut);
        buttonViewContracts = findViewById(R.id.buttonViewContracts);

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Check if GOOGLE_WEB_CLIENT_ID is present
        if (TextUtils.isEmpty(BuildConfig.GOOGLE_WEB_CLIENT_ID)) {
            Log.e("AdminDashboardActivity", "Google Web Client ID is missing");
            Toast.makeText(this, "Error: Google Sign-In configuration is missing.", Toast.LENGTH_LONG).show();
            return;
        }

        // Initialize Google Sign-In client
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .requestEmail()
                .build();
        googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this, gso);

        // Set default fragment to AddCarFragment
        replaceFragment(new AddCarFragment());

        // Set button click listeners to replace fragments
        buttonAddCar.setOnClickListener(v -> replaceFragment(new AddCarFragment()));
        buttonViewCars.setOnClickListener(v -> replaceFragment(new ViewCarsFragment()));
        buttonViewContracts.setOnClickListener(v -> replaceFragment(new ViewContractsFragment()));
        btnSignout.setOnClickListener(v -> SignOutActivity.signOut(this, mAuth, googleSignInClient));
    }

    // Function to replace fragments
    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.adminFragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }
}
