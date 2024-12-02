package com.example.carrentalapp.utilities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.carrentalapp.BuildConfig;
import com.example.carrentalapp.R;
import com.example.carrentalapp.factories.UserFactory;
import com.example.carrentalapp.models.User;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class RegisterActivity extends AppCompatActivity {

    private EditText firstNameEditText, lastNameEditText, dobEditText, phoneNumberEditText, driverLicenseIdEditText,
            emailEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton;
    private TextView loginLink;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private static final String CANADIAN_PHONE_PATTERN = "^(\\+1[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}$";
    private static final String DRIVER_LICENSE_PATTERN = "^\\d{6}$";
    private static final String PASSWORD_PATTERN = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[!@#$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#$%^&*(),.?\":{}|<>]{8,}$";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private TextView passwordStrengthText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firstNameEditText = findViewById(R.id.firstName);
        lastNameEditText = findViewById(R.id.lastName);
        dobEditText = findViewById(R.id.dobEditText);
        phoneNumberEditText = findViewById(R.id.phoneNumber);
        driverLicenseIdEditText = findViewById(R.id.driverLicenseId);
        emailEditText = findViewById(R.id.emailRegistration);
        passwordEditText = findViewById(R.id.passwordRegistration);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordRegistration);
        registerButton = findViewById(R.id.registerbtn);
        loginLink = findViewById(R.id.loginLink);
        passwordStrengthText = findViewById(R.id.passwordStrengthText);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        dobEditText.setOnClickListener(v -> showDatePickerDialog());
        loginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
        registerButton.setOnClickListener(v -> registerUser());

        // real-time check for password
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String password = charSequence.toString();

                if (!password.isEmpty()) {
                    passwordStrengthText.setVisibility(View.VISIBLE);  // Show text
                } else {
                    passwordStrengthText.setVisibility(View.GONE);  // Hide text when empty
                }

                int strength = getPasswordStrength(password);
                updatePasswordStrengthUI(strength);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });


    }

    private void updatePasswordStrengthUI(int strength) {
        switch (strength) {
            case 0:
                passwordStrengthText.setText("Very Weak");
                passwordStrengthText.setTextColor(getResources().getColor(R.color.holo_red_dark));
                break;
            case 1:
            case 2:
                passwordStrengthText.setText("Weak");
                passwordStrengthText.setTextColor(getResources().getColor(R.color.secondaryDarkColor));
                break;
            case 3:
                passwordStrengthText.setText("Good");
                passwordStrengthText.setTextColor(getResources().getColor(R.color.secondaryColor)); // Yellow for good
                break;
            case 4:
                passwordStrengthText.setText("Strong");
                passwordStrengthText.setTextColor(getResources().getColor(R.color.secondaryLightColor)); // Green for strong
                break;
            case 5:
                passwordStrengthText.setText("Very Strong");
                passwordStrengthText.setTextColor(getResources().getColor(R.color.colorActive));
                break;
        }
    }

    private int getPasswordStrength(String password) {
        int strength = 0;

        // Check for length (at least 8 characters)
        if (password.length() >= 8) strength++;

        // Check for at least one uppercase letter
        if (password.matches(".*[A-Z].*")) strength++;

        // Check for at least one lowercase letter
        if (password.matches(".*[a-z].*")) strength++;

        // Check for at least one number
        if (password.matches(".*[0-9].*")) strength++;

        // Check for at least one special character
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) strength++;

        // Return the calculated strength, with a max of 5 for strong password criteria
        return strength;
    }

    private void registerUser() {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String dob = dobEditText.getText().toString().trim();
        String phoneNumber = phoneNumberEditText.getText().toString().trim();
        String driverLicenseId = driverLicenseIdEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Validate inputs in UI order
        if (TextUtils.isEmpty(firstName)) {
            firstNameEditText.setError("First Name is required");
            firstNameEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(lastName)) {
            lastNameEditText.setError("Last Name is required");
            lastNameEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(dob)) {
            dobEditText.setError("Date of Birth is required");
            dobEditText.requestFocus();
            return;
        }

        if (!isAgeValid(dob)) {
            dobEditText.setError("You must be at least 18 years old to register");
            dobEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            phoneNumberEditText.setError("Phone Number is required");
            phoneNumberEditText.requestFocus();
            return;
        }

        if (!phoneNumber.matches(CANADIAN_PHONE_PATTERN)) {
            phoneNumberEditText.setError("Invalid Canadian Phone Number");
            phoneNumberEditText.requestFocus();
            return;
        }

        if (!TextUtils.isEmpty(driverLicenseId) && !driverLicenseId.matches(DRIVER_LICENSE_PATTERN)) {
            driverLicenseIdEditText.setError("Driver License ID must be 6 digits");
            driverLicenseIdEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Invalid Email");
            emailEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return;
        }

        if (!password.matches(PASSWORD_PATTERN)) {
            passwordEditText.setError("Password must be at least 8 characters long, contain 1 uppercase, 1 lowercase, 1 special character, and no spaces.");
            passwordEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordEditText.setError("Confirm Password is required");
            confirmPasswordEditText.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            confirmPasswordEditText.requestFocus();
            return;
        }

        // Proceed with Firebase registration if all validations pass
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();

                        // Use UserFactory to create a customer user
                        User newUser = UserFactory.createUser(
                                "customer",
                                uid,
                                firstName,
                                lastName,
                                email,
                                phoneNumber,
                                driverLicenseId,
                                Timestamp.now(),
                                Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.ic_user_avatar_placeholder).toString()
                        );

                        saveUserDataToFirestore(newUser);
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isAgeValid(String dob) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            Date birthDate = sdf.parse(dob);
            Calendar today = Calendar.getInstance();
            Calendar dobCalendar = Calendar.getInstance();
            dobCalendar.setTime(birthDate);

            int age = today.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR);

            if (today.get(Calendar.DAY_OF_YEAR) < dobCalendar.get(Calendar.DAY_OF_YEAR)) {
                age--; // If today's date is before the user's birthday in the current year, reduce the age
            }

            return age >= 18; // Return true if age is 18 or above
        } catch (ParseException e) {
            dobEditText.setError("Invalid Date Format. Use yyyy-MM-dd.");
            dobEditText.requestFocus();
            return false;
        }
    }

    private void saveUserDataToFirestore(User newUser) {
        db.collection("Users").document(newUser.getUid()).set(newUser)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR) - 18; // Minimum age 18
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                RegisterActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> dobEditText.setText(String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)),
                year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis()); // Disable future dates
        datePickerDialog.show();
    }
}