package com.example.carrentalapp.common;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.carrentalapp.R;
import com.example.carrentalapp.customer.CustomerDashboardActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttp;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RentCarFragment extends Fragment {

    private EditText startDateTimeEditText, endDateTimeEditText;
    private Button rentButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private GoogleCalendarHelper calendarHelper;
    private String carId;
    private double pricePerDay;
    private double totalPayment;
    private Date startDate, endDate;
    //-----------stripe
    private PaymentSheet paymentSheet;
    private String paymentIntentClientSecret;
    private String publishableKey;
    private OkHttpClient client = new OkHttpClient();
    private PaymentSheet.CustomerConfiguration customerConfig;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        initializeCalendarHelper();


        //---------stripe - Initialize Stripe SDK with the publishableKey
        publishableKey = getString(R.string.stripe_publishable_key);
        PaymentConfiguration.init(getContext(), publishableKey);
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rent_car, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind views
        TextView carBrandModelTextView = view.findViewById(R.id.carBrandModel);
        TextView carLocationTextView = view.findViewById(R.id.carLocation);
        TextView carSeatsTextView = view.findViewById(R.id.carSeats);
        TextView carPriceTextView = view.findViewById(R.id.carPrice);
        RatingBar carRatingBar = view.findViewById(R.id.carRating);
        TextView ratingCountTextView = view.findViewById(R.id.ratingCount);
        ImageView carImage = view.findViewById(R.id.carImage);
        startDateTimeEditText = view.findViewById(R.id.startDateTimeEditText);
        endDateTimeEditText = view.findViewById(R.id.endDateTimeEditText);
        rentButton = view.findViewById(R.id.rentButton);

        // Retrieve car data from arguments
        Bundle bundle = getArguments();
        if (bundle != null) {
            carId = bundle.getString("carId");
            String carBrandModel = bundle.getString("carBrandModel");
            String carLocation = bundle.getString("carLocation");
            int carSeats = bundle.getInt("carSeats");
            pricePerDay = bundle.getDouble("carPrice");
            float carRating = bundle.getFloat("carRating");
            String carImageUrl = bundle.getString("carImageUrl");

            // Populate UI elements with car data
            carBrandModelTextView.setText(carBrandModel);
            carLocationTextView.setText("Location: " + carLocation);
            carSeatsTextView.setText("Seats: " + carSeats);
            carPriceTextView.setText("Price per day: $" + pricePerDay);
            carRatingBar.setRating(carRating);
            ratingCountTextView.setText(String.format("%.1f", carRating));

            if (carImageUrl != null) {
                Glide.with(this).load(carImageUrl).into(carImage);
            }
        }

        startDateTimeEditText.setOnClickListener(v -> showDateTimePicker(startDateTimeEditText));
        endDateTimeEditText.setOnClickListener(v -> showDateTimePicker(endDateTimeEditText));

        rentButton.setOnClickListener(v -> rentCar());
    }


    private void rentCar() {
        String startDateStr = startDateTimeEditText.getText().toString();
        String endDateStr = endDateTimeEditText.getText().toString();

        startDate = parseDate(startDateStr);
        endDate = parseDate(endDateStr);

        if (startDate == null || endDate == null || endDate.before(startDate)) {
            Toast.makeText(getContext(), "Please enter valid start and end dates", Toast.LENGTH_SHORT).show();
            return;
        }


        // Calculate total payment based on the rental period
        totalPayment = pricePerDay * ((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24));

        Log.d("RentCarFragment", "Renting car with ID: " + carId);
        fetchPaymentSheetConfig();

    }

    private void createEventAndSaveContract(Date startDate, Date endDate) {
        new Thread(() -> {
            String eventId = "failed_event";
            try {
                eventId = calendarHelper.createEvent("Rent Car ID: " + carId, startDate, endDate);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Calendar event created successfully!", Toast.LENGTH_SHORT).show()
                    );
                }
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Failed to create calendar event: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            }

            saveContractToFirestore(eventId, startDate, endDate); // Save the contract regardless of calendar event success
        }).start();
    }


    private void saveContractToFirestore(String eventId, Date startDate, Date endDate) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                );
            }
            return;
        }

        String userId = currentUser.getUid();
        totalPayment = pricePerDay * ((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24));

        Map<String, Object> contractData = new HashMap<>();
        contractData.put("userId", userId);
        contractData.put("carId", carId);
        contractData.put("startDate", new Timestamp(startDate));
        contractData.put("endDate", new Timestamp(endDate));
        contractData.put("totalPayment", totalPayment);
        contractData.put("status", ContractStatus.ACTIVE);
        contractData.put("eventId", eventId);

        db.collection("Contracts").add(contractData)
                .addOnSuccessListener(documentReference -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Contract saved successfully.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to save contract: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Date parseDate(String dateString) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showDateTimePicker(EditText dateTimeEditText) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            getContext(),
                            (timeView, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);
                                dateTimeEditText.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(calendar.getTime()));
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true);
                    timePickerDialog.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void initializeCalendarHelper() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("CarRentalAppPrefs", Context.MODE_PRIVATE);
        String accountName = sharedPreferences.getString("google_account_name", null);

        if (accountName != null) {
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                    getContext(), Collections.singleton(CalendarScopes.CALENDAR));
            credential.setSelectedAccountName(accountName);
            calendarHelper = new GoogleCalendarHelper(credential);
            Log.d("RentCarFragment", "Google Calendar credential initialized with account: " + accountName);
        } else {
            Log.e("RentCarFragment", "Google account name is missing; cannot initialize Google Calendar.");
            Toast.makeText(getContext(), "Google Calendar setup failed. Please re-login.", Toast.LENGTH_SHORT).show();
        }
    }

    // Navigate to Customer Dashboard
    private void navigateToCustomerDashboard() {
        Intent intent = new Intent(getActivity(), CustomerDashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear back stack
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    //-------Stripe: make a network request to the server

    private void fetchPaymentSheetConfig() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        String email = currentUser.getEmail();
        String name = currentUser.getDisplayName();


        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("name", name);
            jsonBody.put("totalAmount", totalPayment * 100);
        } catch(JSONException e) {
            e.printStackTrace();
            return;
        }
        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url("http://10.0.2.2:4242/payment-sheet") // Emulator endpoint for localhost
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Failed to fetch payment config", Toast.LENGTH_SHORT).show()
                    );
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject responseJson = new JSONObject(response.body().string());
                        paymentIntentClientSecret = responseJson.getString("paymentIntent");
                        String customerId = responseJson.getString("customer");
                        String ephemeralKey = responseJson.getString("ephemeralKey");
                        String publishableKey = responseJson.getString("publishableKey");

                        // Log and Toast the fetched information
                        Log.d("RentCarFragment", "Fetched payment configuration successfully.");
                        Log.d("RentCarFragment", "paymentIntentClientSecret: " + paymentIntentClientSecret);
                        Log.d("RentCarFragment", "customerId: " + customerId);
                        Log.d("RentCarFragment", "ephemeralKey: " + ephemeralKey);
                        Log.d("RentCarFragment", "publishableKey: " + publishableKey);

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "Payment config fetched successfully", Toast.LENGTH_SHORT).show()
                            );
                        }

                        // Initialize PaymentConfiguration with the publishableKey
                        PaymentConfiguration.init(getContext(), publishableKey);

                        // Set up customerConfig for PaymentSheet
                        customerConfig = new PaymentSheet.CustomerConfiguration(customerId, ephemeralKey);

                        // After fetching, call the method to present PaymentSheet
                        presentPaymentSheet();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Error: Unable to fetch payment config", Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            }
        });
    }

    private void presentPaymentSheet() {
        if (customerConfig != null && paymentIntentClientSecret != null) {
            paymentSheet.presentWithPaymentIntent(
                    paymentIntentClientSecret,
                    new PaymentSheet.Configuration(
                            "CarRentalApp", // Business name displayed on PaymentSheet
                            customerConfig
                    )
            );
        }
    }

    private void onPaymentSheetResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            // Payment successful
            if (getActivity() != null) { // Null check for the activity context
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Payment Successful!", Toast.LENGTH_SHORT).show();

                    // Call the method to create contract and calendar event after successful payment
                    createEventAndSaveContract(startDate, endDate);

                    // Navigate to the dashboard after successful payment
                    navigateToCustomerDashboard();
                });
            }
        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            if (getActivity() != null) { // Null check
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Payment Canceled", Toast.LENGTH_SHORT).show()
                );
            }
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            if (getActivity() != null) { // Null check
                getActivity().runOnUiThread(() -> {
                    PaymentSheetResult.Failed failedResult = (PaymentSheetResult.Failed) paymentSheetResult;
                    Toast.makeText(getContext(), "Payment Failed: " + failedResult.getError(), Toast.LENGTH_SHORT).show();
                });
            }
        }
    }





//    private void testGoogleCalendarEventCreation() {
//        new Thread(() -> {
//            try {
//                // Hardcoded event title and date values for testing
//                String eventTitle = "Test Event";
//                Date startDate = new Date();  // Current date and time
//                Date endDate = new Date(System.currentTimeMillis() + 3600000); // 1 hour after start date
//
//                Log.d("Test Event", "Attempting to create event with Title: " + eventTitle);
//                Log.d("Test Event", "Start Date: " + startDate);
//                Log.d("Test Event", "End Date: " + endDate);
//
//                // Attempt to create the Google Calendar event
//                String eventId = calendarHelper.createEvent(eventTitle, startDate, endDate);
//                Log.d("Test Event", "Event created with ID: " + eventId);
//
//                // Notify user if successful
//                if (getActivity() != null) {
//                    getActivity().runOnUiThread(() ->
//                            Toast.makeText(getContext(), "Test event created successfully with ID: " + eventId, Toast.LENGTH_SHORT).show()
//                    );
//                }
//            } catch (Exception e) {
//                // Log and show error message if the event creation fails
//                Log.e("Test Event", "Failed to create test event", e);
//                if (getActivity() != null) {
//                    getActivity().runOnUiThread(() ->
//                            Toast.makeText(getContext(), "Failed to create test event: " + e.getMessage(), Toast.LENGTH_SHORT).show()
//                    );
//                }
//            }
//        }).start();
//    }

    //-----stipe call API and retrieve client_secret


}