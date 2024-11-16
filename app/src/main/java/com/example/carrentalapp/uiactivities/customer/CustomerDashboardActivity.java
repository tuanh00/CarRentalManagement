package com.example.carrentalapp.uiactivities.customer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.carrentalapp.BuildConfig;
import com.example.carrentalapp.R;
import com.example.carrentalapp.uiactivities.admin.ViewContractsFragment;
import com.example.carrentalapp.utilities.SignOutActivity;
import com.example.carrentalapp.common.CarAdapter;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

public class CustomerDashboardActivity extends AppCompatActivity {
    private EditText searchBar;
    private TextView textViewGreeting;
    private Button searchButton, signOutButton, viewContractsButton;
    private static final String PREFS_NAME = "CarRentalAppPrefs";
    private static final String ROLE_KEY = "user_role";
    private CarAdapter carAdapter;
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private ViewAvailableCarFragment viewAvailableCarFragment; // Reference to the fragment

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_rental_app);

        // Initialize Firebase and Google SignIn
        mAuth = FirebaseAuth.getInstance();

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.customerToolbar);
        setSupportActionBar(toolbar);

        //Set greeting text
        textViewGreeting = findViewById(R.id.textViewGreeting);
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String firstName = sharedPreferences.getString("first_name", "User");
        textViewGreeting.setText("Hi, " + firstName);

        // Initialize UI elements
        searchBar = findViewById(R.id.searchBar);
        searchButton = findViewById(R.id.searchButton);
        viewContractsButton = findViewById(R.id.buttonViewContracts);
        signOutButton = findViewById(R.id.signOutButton);

        // Load ViewAvailableCarFragment
        viewAvailableCarFragment = new ViewAvailableCarFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.customerFragmentContainer, viewAvailableCarFragment)
                .commit();

        // Set up Google SignIn
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .requestEmail()
                .build();
        googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this, gso);

        //Set up listensers
        searchButton.setOnClickListener(v -> {
            String searchText = searchBar.getText().toString().toLowerCase();
            if (viewAvailableCarFragment != null) {
                viewAvailableCarFragment.filterCars(searchText);
            }
        });

        signOutButton.setOnClickListener(v -> SignOutActivity.signOut(this, mAuth, googleSignInClient));

        viewContractsButton.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.customerFragmentContainer, new ViewContractsFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Adjust UI visibility based on fragment stack
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            boolean hasFragments = getSupportFragmentManager().getBackStackEntryCount() > 0;
            toggleUIVisibility(!hasFragments);
        });
    }

    // Toggle visibility of UI components based on fragment visibility
    private void toggleUIVisibility(boolean isVisible) {
        searchBar.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        searchButton.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        signOutButton.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        viewContractsButton.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        textViewGreeting.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }
}