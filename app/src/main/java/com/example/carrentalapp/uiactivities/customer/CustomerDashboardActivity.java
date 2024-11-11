package com.example.carrentalapp.uiactivities.customer;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carrentalapp.BuildConfig;
import com.example.carrentalapp.R;
import com.example.carrentalapp.common.ViewContractsFragment;
import com.example.carrentalapp.states.car.CarAvailabilityState;
import com.example.carrentalapp.utilities.SignOutActivity;
import com.example.carrentalapp.models.Car;
import com.example.carrentalapp.common.CarAdapter;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;

public class CustomerDashboardActivity extends AppCompatActivity {
    private RecyclerView recyclerViewCars, recyclerViewContracts;
    private EditText searchBar, fromDateTimePicker, toDateTimePicker;
    private Button searchButton, signOutButton, viewContractsButton ;
    private FirebaseFirestore database;
    private CarAdapter carAdapter;
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private ArrayList<Car> searchedCars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_rental_app);

        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.customerToolbar);
        setSupportActionBar(toolbar);

        SharedPreferences sharedPreferences = getSharedPreferences("CarRentalAppPrefs", MODE_PRIVATE);
        String role = sharedPreferences.getString("user_role", "no role found");
        Log.d("CustomerDashboardActivity", "User role in Customer Dashboard: " + role);


        // listViewCars = findViewById(R.id.listViewCars);
        recyclerViewCars = findViewById(R.id.recyclerViewCars);
        searchBar = findViewById(R.id.searchBar);
       // fromDateTimePicker = findViewById(R.id.fromDateTimePicker);
        //toDateTimePicker = findViewById(R.id.toDateTimePicker);
        searchButton = findViewById(R.id.searchButton);
        viewContractsButton = findViewById(R.id.buttonViewContracts);
        signOutButton = findViewById(R.id.signOutButton);

        database = FirebaseFirestore.getInstance();
        searchedCars = new ArrayList<>();
        carAdapter = new CarAdapter(this, searchedCars);
        recyclerViewCars.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCars.setAdapter(carAdapter);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .requestEmail()
                .build();
        googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this, gso);

        loadCarDatas();

        // Set up listerning for Datetime Picker and Search
       // fromDateTimePicker.setOnClickListener(v -> showDatePickerDialog(fromDateTimePicker));
        //toDateTimePicker.setOnClickListener(v -> showDatePickerDialog(toDateTimePicker));
        searchButton.setOnClickListener(v -> filterCars());
        //Set sign-out button event
        signOutButton.setOnClickListener(v -> SignOutActivity.signOut(this, mAuth, googleSignInClient));
// Inside onCreate() in CustomerDashboardActivity.java

        viewContractsButton.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.customerFragmentContainer, new ViewContractsFragment())
                    .addToBackStack(null)
                    .commit();
        });

        //listen for fragment stack changes, refresh fragment UI
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            boolean hasFragments = getSupportFragmentManager().getBackStackEntryCount() > 0;
            recyclerViewCars.setVisibility(hasFragments ? View.GONE : View.VISIBLE);
            searchBar.setVisibility(hasFragments ? View.GONE : View.VISIBLE);
            //fromDateTimePicker.setVisibility(hasFragments ? View.GONE : View.VISIBLE);
            //toDateTimePicker.setVisibility(hasFragments ? View.GONE : View.VISIBLE);
            searchButton.setVisibility(hasFragments ? View.GONE : View.VISIBLE);
            signOutButton.setVisibility(hasFragments ? View.GONE : View.VISIBLE);

            findViewById(R.id.customerFragmentContainer).setVisibility(hasFragments ? View.VISIBLE : View.GONE);

        });
    }

    private void loadCarDatas() {
        database.collection("Cars").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        searchedCars.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Car car = document.toObject(Car.class);
                            car.setId(document.getId()); // Set the document ID as carId in the Car object

                            // Log car information to debug
                            Log.d("CustomerDashboardActivity", "Loaded car: " + car.getBrand() + " " + car.getModel());
                            Log.d("CustomerDashboardActivity", "Car state: " + car.getCurrentState().name());

                            // Display cars based on availability
                            if (car.getCurrentState().name().equals(CarAvailabilityState.AVAILABLE.name())) {
                                searchedCars.add(car);
                                Log.d("CustomerDashboardActivity", "Added to searchedCars: " + car.getBrand() + " " + car.getModel());
                            } else {
                                Log.d("CustomerDashboardActivity", "Skipped (Unavailable): " + car.getBrand() + " " + car.getModel());
                            }
                        }
                        carAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("CustomerDashboardActivity", "Error getting documents.", task.getException());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CustomerDashboardActivity", "Failed to retrieve cars", e);
                });
    }

    private void filterCars() {
        String searchText = searchBar.getText().toString().toLowerCase();
        ArrayList<Car> filteredCars = new ArrayList<>();

        for (Car car : searchedCars) {
            if (car.getBrand().toLowerCase().contains(searchText) ||
                    car.getModel().toLowerCase().contains(searchText) ||
                    String.valueOf(car.getSeats()).contains(searchText) ||
                    String.valueOf(car.getPrice()).contains(searchText) ||
                    String.valueOf(car.getRating()).contains(searchText)) {

                filteredCars.add(car);
            }
        }
        //updateListView(carList);
        carAdapter = new CarAdapter(this, filteredCars);
        recyclerViewCars.setAdapter(carAdapter);
    }

//    private void showDatePickerDialog(EditText datePickerEditText) {
//        final Calendar calendar = Calendar.getInstance();
//        int year = calendar.get(Calendar.YEAR);
//        int month = calendar.get(Calendar.MONTH);
//        int day = calendar.get(Calendar.DAY_OF_MONTH);
//
//        DatePickerDialog datePickerDialog = new DatePickerDialog(
//                CustomerDashboardActivity.this,
//                (view, selectedYear, selectedMonth, selectedDay) -> datePickerEditText.setText(String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)),
//                year, month, day);
//
//        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
//        datePickerDialog.show();
//
//
//
//    }
}