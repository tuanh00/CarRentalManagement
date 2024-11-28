package com.example.carrentalapp.utilities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
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

import com.example.carrentalapp.factories.UserFactory;
import com.example.carrentalapp.models.User;
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
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "CarRentalAppPrefs";
    private static final String ROLE_KEY = "user_role";
    private static final String GOOGLE_ACCOUNT_NAME_KEY = "google_account_name";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int RC_SIGN_IN = 9001;
    private static final List<String> ADMIN_EMAILS = Arrays.asList(
            "chtuanh265@gmail.com", "admin2@gmail.com", "duy.roan@gmail.com"
    );

    private GoogleSignInClient googleSignInClient;
    private EditText emailEditText, passwordEditText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .requestEmail()
                .requestScopes(new Scope(CalendarScopes.CALENDAR))
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
                        handleGoogleUser(user);
                    } else {
                        Toast.makeText(this, "Firebase Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleGoogleUser(FirebaseUser firebaseUser) {
        db.collection("Users").document(firebaseUser.getUid()).get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) {
                        String role = ADMIN_EMAILS.contains(firebaseUser.getEmail()) ? "admin" : "customer";
                        String firstName = firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName().split(" ")[0] : "";
                        String lastName = firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName().split(" ")[1] : "";

                        User newUser = UserFactory.createUser(
                                role,
                                firebaseUser.getUid(),
                                firstName,
                                lastName,
                                firebaseUser.getEmail(),
                                null, // No phone number
                                null, // No driver license ID for Google Sign-In
                                Timestamp.now(),
                                Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.ic_user_avatar_placeholder).toString()
                        );

                        saveUserToFirestore(newUser);
                    } else {
                        String role = document.getString("role");
                        String firstName = document.getString("firstName");
                        String lastName = document.getString("lastName");
                        String email = document.getString("email");

                        // Save user details to SharedPreferences
                        saveUserToPreferences(firebaseUser.getUid(), email, role, firstName, lastName);

                        navigateToDashboard(role);
                    }
                })
                .addOnFailureListener(e -> Log.e("LoginActivity", "Error fetching user data", e));
    }


    private void saveUserToFirestore(User user) {
        db.collection("Users").document(user.getUid()).set(user)
                .addOnSuccessListener(aVoid -> {
                    saveUserToPreferences(user.getUid(), user.getEmail(), user.getRole(), user.getFirstName(), user.getLastName());
                    navigateToDashboard(user.getRole());
                })
                .addOnFailureListener(e -> Log.e("LoginActivity", "Error saving user data", e));
    }


    private void handleExistingUser(FirebaseUser firebaseUser, String role, Boolean isBlocked) {
        if (isBlocked != null && isBlocked) {
            Toast.makeText(this, "Your account is blocked. Contact support.", Toast.LENGTH_LONG).show();
            mAuth.signOut();
            return;
        }

        User existingUser = UserFactory.createUser(
                role,
                firebaseUser.getUid(),
                firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName().split(" ")[0] : "",
                firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName().split(" ")[1] : "",
                firebaseUser.getEmail(),
                null, // No phone number
                null, // No driver license ID
                Timestamp.now(),
                Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.ic_user_avatar_placeholder).toString()
        );

        // Extract fields and save to preferences
        saveUserToPreferences(
                existingUser.getUid(),
                existingUser.getEmail(),
                existingUser.getRole(),
                existingUser.getFirstName(),
                existingUser.getLastName()
        );

        navigateToDashboard(role);
    }

    private void saveUserToPreferences(String userId, String email, String role, String firstName, String lastName) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_id", userId);
        editor.putString("email", email);
        editor.putString(ROLE_KEY, role);
        editor.putString("first_name", firstName);
        editor.putString("last_name", lastName);
        editor.putString("last_name", lastName);
        editor.putLong("createdAt", System.currentTimeMillis());
        editor.putString(GOOGLE_ACCOUNT_NAME_KEY, email); // Save the Google account name
        editor.apply();
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
                            Toast.makeText(this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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

    private void navigateUser(FirebaseUser firebaseUser, String provider) {
        db.collection("Users").document(firebaseUser.getUid()).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String role = document.getString("role");
                        String firstName = document.getString("firstName");
                        String lastName = document.getString("lastName");
                        String email = document.getString("email");

                        // Save user details to SharedPreferences
                        saveUserToPreferences(firebaseUser.getUid(), email, role, firstName, lastName);

                        // Navigate to the appropriate dashboard
                        navigateToDashboard(role);
                    } else {
                        Toast.makeText(this, "User profile not found. Please register.", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                    }
                })
                .addOnFailureListener(e -> Log.e("LoginActivity", "Error fetching user data", e));
    }

    private void navigateToDashboard(String role) {
        Intent intent = "admin".equals(role)
                ? new Intent(this, AdminDashboardActivity.class)
                : new Intent(this, CustomerDashboardActivity.class);
        startActivity(intent);
        finish();
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
