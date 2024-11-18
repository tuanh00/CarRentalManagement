// CustomerDashboardActivity.java
package com.example.carrentalapp.uiactivities.customer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.carrentalapp.BuildConfig;
import com.example.carrentalapp.R;
import com.example.carrentalapp.common.ProfileFragment;
import com.example.carrentalapp.uiactivities.admin.ViewContractsFragment;
import com.example.carrentalapp.uiactivities.customer.ViewAvailableCarFragment;
import com.example.carrentalapp.utilities.SignOutActivity;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class CustomerDashboardActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private BottomNavigationView bottomNavigationView;
    private androidx.appcompat.widget.Toolbar toolbar;
    private TextView textViewGreeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_dashboard); // Correct layout reference

        // Initialize Firebase and Google SignIn
        mAuth = FirebaseAuth.getInstance();

        // Set up the toolbar
        setupToolbar();

        // Set greeting text
        textViewGreeting = findViewById(R.id.textViewGreeting);
        SharedPreferences sharedPreferences = getSharedPreferences("CarRentalAppPrefs", MODE_PRIVATE);
        String firstName = sharedPreferences.getString("first_name", "User");
        textViewGreeting.setText("Hi, " + firstName);
        Log.d("CustomerDashboard", "Retrieved firstName: " + firstName); // Debugging

        // Initialize BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottomNavigationView); // Initialize BottomNavigationView

        // Load ViewAvailableCarFragment as default
        if (savedInstanceState == null) {
            replaceFragment(new ViewAvailableCarFragment());
            bottomNavigationView.setSelectedItemId(R.id.navigation_view_available_cars);
        }

        // Set up Google SignIn
        setupGoogleSignInClient();

        // Set up BottomNavigationView
        setupBottomNavigationView();

        // Set up listeners (now minimal)
        setupListeners();

        // Customize status bar if needed
        // customizeStatusBar(); // Removed for global styling
    }

    /**
     * Sets up the Toolbar with custom styles and title.
     */
    private void setupToolbar() {
        toolbar = findViewById(R.id.customerToolbar);
        setSupportActionBar(toolbar);
    }

    /**
     * Initializes the Google Sign-In client.
     */
    private void setupGoogleSignInClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .requestEmail()
                .build();
        googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this, gso);
    }

    /**
     * Sets up the BottomNavigationView with an item selected listener.
     */
    private void setupBottomNavigationView() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            Log.d("CustomerDashboard", "Selected Item ID: " + itemId);

            if (itemId == R.id.navigation_view_available_cars) {
                Log.d("CustomerDashboard", "Loading ViewAvailableCarFragment");
                selectedFragment = new ViewAvailableCarFragment();
            } else if (itemId == R.id.navigation_profile) {
                Log.d("CustomerDashboard", "Loading ProfileFragment");
                selectedFragment = new ProfileFragment();
            } else if (itemId == R.id.navigation_view_available_contracts) {
                Log.d("CustomerDashboard", "Loading ViewContractsFragment");
                selectedFragment = new ViewContractsFragment();
            }
            else if (itemId == R.id.navigation_sign_out) {
                Log.d("CustomerDashboard", "Handling Sign Out");
                // Handle sign out logic
                SignOutActivity.signOut(this, mAuth, googleSignInClient);
                return true; // Event handled
            }

            return loadFragment(selectedFragment);
        });
    }

    /**
     * Sets up listeners for UI elements.
     */
    private void setupListeners() {
        // No search bar listeners in activity now
    }

    /**
     * Loads the selected fragment into the container.
     */
    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.customerFragmentContainer, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    /**
     * Function to replace fragments and add to back stack.
     */
    private void replaceFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.customerFragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    /**
     * Removed toggleUIVisibility function
     */
}
