// AdminDashboardActivity.java
package com.example.carrentalapp.uiactivities.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.carrentalapp.BuildConfig;
import com.example.carrentalapp.R;
import com.example.carrentalapp.utilities.SignOutActivity;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Setup Toolbar
        setupToolbar();

        // Check if GOOGLE_WEB_CLIENT_ID is present
        if (TextUtils.isEmpty(BuildConfig.GOOGLE_WEB_CLIENT_ID)) {
            Log.e("AdminDashboardActivity", "Google Web Client ID is missing");
            Toast.makeText(this, "Error: Google Sign-In configuration is missing.", Toast.LENGTH_LONG).show();
            return;
        }

        // Initialize Google Sign-In client
        setupGoogleSignInClient();

        // Initialize BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        setupBottomNavigationView();

        // Set default fragment to AddCarFragment
        if (savedInstanceState == null) {
            replaceFragment(new AddCarFragment());
            bottomNavigationView.setSelectedItemId(R.id.navigation_add_car);
        }
    }

    /**
     * Sets up the Toolbar with custom styles and title.
     */
    private void setupToolbar() {
        // Toolbar is already set in XML with proper styling
        // No additional setup needed unless dynamic changes are required
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
     * Sets up the BottomNavigationView with an item selected listener using if-else statements.
     */
    private void setupBottomNavigationView() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_add_car) {
                selectedFragment = new AddCarFragment();
            } else if (itemId == R.id.navigation_view_cars) {
                selectedFragment = new ViewCarsFragment();
            } else if (itemId == R.id.navigation_view_contracts) {
                selectedFragment = new ViewContractsFragment();
            } else if (itemId == R.id.navigation_view_users) {
                selectedFragment = new ViewUsersFragment();
            }
            else if (itemId == R.id.navigation_sign_out) {
                // Handle sign out logic
                SignOutActivity.signOut(this, mAuth, googleSignInClient);
                return true; // Return true as we've handled the event
            }

            return loadFragment(selectedFragment);
        });
    }

    /**
     * Loads the selected fragment into the container.
     */
    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.adminFragmentContainer, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    // Function to replace fragments
    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.adminFragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Handles the sign-out process.
     */
    // Sign-Out is handled via SignOutActivity
}
