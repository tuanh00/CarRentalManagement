package com.example.carrentalapp.utilities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.carrentalapp.uiactivities.admin.AdminDashboardActivity;
import com.example.carrentalapp.uiactivities.customer.CustomerDashboardActivity;
import com.example.carrentalapp.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.services.calendar.CalendarScopes;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.example.carrentalapp.BuildConfig;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "CarRentalAppPrefs";
    private static final String ROLE_KEY = "user_role";
    private static final String GOOGLE_ACCOUNT_NAME_KEY = "google_account_name";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int RC_SIGN_IN = 9001;
    private static final List<String> ADMIN_EMAILS = Arrays.asList(
            "chtuanh265@gmail.com", // Add any additional admin emails here
            "admin2@gmail.com", "duy.roan@gmail.com"
    );

    private GoogleSignInClient googleSignInClient;
    private EditText emailEditText, passwordEditText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeUI();
        initializeGoogleSignIn();
        checkLocationPermission();
    }

    private void initializeUI() {
        emailEditText = findViewById(R.id.emailLogin);
        passwordEditText = findViewById(R.id.passwordLogin);
        Button loginButton = findViewById(R.id.loginbtn);
        Button registerLink = findViewById(R.id.registerLink);
        Button googleSignButton = findViewById(R.id.googleSignInButton);

        registerLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });

        loginButton.setOnClickListener(v -> loginUser());
        googleSignButton.setOnClickListener(v -> signInWithGoogle());
    }

    private void initializeGoogleSignIn() {
        // Initialize Google Sign-in options
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .requestEmail()
                .requestScopes(new Scope(CalendarScopes.CALENDAR)) // Request Calendar scope
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signInWithGoogle() {
        googleSignInClient.revokeAccess().addOnCompleteListener(task -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int ResultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, ResultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult();
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (Exception e) {
                Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        checkAndSaveGoogleUser(user);
                    } else {
                        Toast.makeText(this, "Firebase Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void checkAndSaveGoogleUser(FirebaseUser user) {
        if (user != null) {
            db.collection("Users").document(user.getUid()).get()
                    .addOnSuccessListener(document -> {
                        if (!document.exists()) {
                            // Determine role based on email
                            String role = ADMIN_EMAILS.contains(user.getEmail()) ? "admin" : "customer";

                            // Prepare user data with additional fields
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("userId", user.getUid());
                            userData.put("email", user.getEmail());
                            userData.put("role", role);
                            userData.put("createdAt", Timestamp.now());
                            userData.put("imgUrl", null);
                            userData.put("blocked", false);

                            String firstName = user.getDisplayName() != null ? user.getDisplayName().split(" ")[0] : "";
                            String lastName = user.getDisplayName() != null ? user.getDisplayName().split(" ")[1] : "";
                            userData.put("firstName", firstName);
                            userData.put("lastName", lastName);
                            userData.put("imgUrl", null);
                            userData.put("blocked", false);

                            // Save user data to Firestore and navigate
                            db.collection("Users").document(user.getUid()).set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        // Save user details to preferences
                                        saveUserDetailsToPreferences(user.getUid(), user.getEmail(), role, firstName, lastName, null);
                                        // Navigate after saving
                                        navigateToDashboard(role);
                                    })
                                    .addOnFailureListener(e -> Log.e("LoginActivity", "Error adding Google user", e));
                        } else {
                            // User already exists in Firestore, retrieve data to save in preferences
                            String role = document.getString("role");
                            Boolean isBlocked = document.getBoolean("blocked");
                            String imgUrl = document.getString("imgUrl");
                            String firstName = document.getString("firstName");
                            String lastName = document.getString("lastName");

                            // Check if user is blocked
                            if (isBlocked != null && isBlocked) {
                                Toast.makeText(this, "Your account has been blocked. Contact support.", Toast.LENGTH_LONG).show();
                                mAuth.signOut();
                                return; // Prevent further execution
                            }

                            saveUserDetailsToPreferences(user.getUid(), user.getEmail(), role, firstName, lastName, imgUrl);
                            // Navigate after retrieving role
                            navigateToDashboard(role);
                        }
                    });
        }
    }
    private void saveUserDetailsToPreferences(String userId, String email, String role, String firstName, String lastName, String imgUrl) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_id", userId);
        editor.putString("email", email);
        editor.putString(ROLE_KEY, role);
        editor.putString("first_name", firstName);
        editor.putString("last_name", lastName);
        editor.putString("imgUrl", imgUrl);
        editor.putString(GOOGLE_ACCOUNT_NAME_KEY, email); // Save the Google account name
        editor.apply();
    }

    /**
     * Determines the navigation flow based on the authentication provider.
     * @param firebaseUser The authenticated Firebase user.
     * @param provider The authentication provider ("google" or "password").
     */
    private void navigateUser(FirebaseUser firebaseUser, String provider) {
        if (firebaseUser != null) {
            if ("google.com".equals(provider)) {
                checkAndSaveGoogleUser(firebaseUser);
            } else {
                checkRoleInFirestore(firebaseUser.getUid());
            }
        }
    }

    /**
     * Checks the user's role and blocked status in Firestore for email/password sign-in.
     */
    private void checkRoleInFirestore(String userId) {
        db.collection("Users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        Boolean isBlocked = documentSnapshot.getBoolean("blocked");
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");
                        String email = documentSnapshot.getString("email");
                        String imgUrl = documentSnapshot.getString("imgUrl");

                        // Check if user is blocked
                        if (isBlocked != null && isBlocked) {
                            Toast.makeText(this, "Your account has been blocked. Contact support.", Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                            return;
                        }

                        // Save user details to preferences
                        saveUserDetailsToPreferences(userId, email, role, firstName, lastName, imgUrl);

                        // Navigate to the appropriate dashboard
                        navigateToDashboard(role);

                    } else {
                        Toast.makeText(this, "User profile not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching user data.", Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToDashboard(String role) {
        if ("admin".equals(role)) {
            navigateToAdminDashboard();
        } else {
            navigateToCustomerDashboard();
        }
    }

    private void navigateToAdminDashboard() {
        Intent intent = new Intent(this, AdminDashboardActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToCustomerDashboard() {
        Intent intent = new Intent(this, CustomerDashboardActivity.class);
        startActivity(intent);
        finish();
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (validateLoginInputs(email, password)) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            navigateUser(firebaseUser, "password");
                        } else {
                            Toast.makeText(LoginActivity.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private boolean validateLoginInputs(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Please enter a valid email");
            emailEditText.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return false;
        }
        return true;
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "Location permission is needed to show nearby cars and distances", Toast.LENGTH_LONG).show();
            }
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission is needed for map features", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}