package com.example.carrentalapp.utilities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.carrentalapp.builders.CustomerBuilder;
import com.example.carrentalapp.builders.IUserBuilder;
import com.example.carrentalapp.builders.UserEngineer;
import com.example.carrentalapp.R;
import com.example.carrentalapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    private EditText firstNameEditText, lastNameEditText, dobEditText, phoneNumberEditText, driverLicenseIdEditText,
            emailEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton, loginLink;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private static final String CANADIAN_PHONE_PATTERN = "^(\\+1[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}$";

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

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        dobEditText.setOnClickListener(v -> showDatePickerDialog());
        loginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
        registerButton.setOnClickListener(v -> registerUser());
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

        // Validate inputs
        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(dob) ||
                TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) ||
                TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!phoneNumber.matches(CANADIAN_PHONE_PATTERN)) {
            phoneNumberEditText.setError("Invalid Canadian Phone Number");
            phoneNumberEditText.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Invalid Email");
            emailEditText.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            confirmPasswordEditText.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();

                        //Use Builder and Factory patterns
                        IUserBuilder userBuilder = new CustomerBuilder();
                        userBuilder.setUid(uid);
                        userBuilder.setFirstName(firstName);
                        userBuilder.setLastName(lastName);
                        userBuilder.setEmail(email);
                        userBuilder.setPhoneNumber(phoneNumber);
                        ((CustomerBuilder) userBuilder).setDriverLicenseId(driverLicenseId);
                        userBuilder.setRole();

                        UserEngineer userEngineer = new UserEngineer(userBuilder);
                        userEngineer.constructUser();
                        User newUser = userEngineer.getUser();

                        saveUserDataToFirestore(newUser);

                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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

    /**
     * Show DatePickerDialog for selecting date of birth.
     */
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
