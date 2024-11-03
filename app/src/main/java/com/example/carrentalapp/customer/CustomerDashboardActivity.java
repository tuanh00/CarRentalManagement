package com.example.carrentalapp.customer;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carrentalapp.R;
import com.example.carrentalapp.auth.SignOutActivity;
import com.example.carrentalapp.common.Car;
import com.example.carrentalapp.common.CarAdapter;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Calendar;

public class CustomerDashboardActivity extends AppCompatActivity {
    //ListView listViewCars;
    private RecyclerView recyclerViewCars;
    private EditText searchBar, fromDateTimePicker, toDateTimePicker;
    private Button searchButton, signOutButton;
    private FirebaseFirestore database;
    private CarAdapter carAdapter;
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;

    //ArrayList<String> carList;
    private ArrayList<Car> searchedCars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_rental_app);

        Toolbar toolbar = findViewById(R.id.customerToolbar);
        setSupportActionBar(toolbar);

        SharedPreferences sharedPreferences = getSharedPreferences("CarRentalAppPrefs", MODE_PRIVATE);
        String role = sharedPreferences.getString("user_role", "no role found");
        Log.d("CustomerDashboardActivity", "User role in Customer Dashboard: " + role);


       // listViewCars = findViewById(R.id.listViewCars);
        recyclerViewCars = findViewById(R.id.recyclerViewCars);
        searchBar = findViewById(R.id.searchBar);
        fromDateTimePicker = findViewById(R.id.fromDateTimePicker);
        toDateTimePicker = findViewById(R.id.toDateTimePicker);
        searchButton = findViewById(R.id.searchButton);
        signOutButton = findViewById(R.id.signOutButton);

        database = FirebaseFirestore.getInstance();
        searchedCars = new ArrayList<>();
        carAdapter = new CarAdapter(this, searchedCars);
        recyclerViewCars.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCars.setAdapter(carAdapter);
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                                .build();
        googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this, gso);

        //carList = new ArrayList<>();

        loadCarDatas();

        // Set up listerning for Datetime Picker and Search
        fromDateTimePicker.setOnClickListener(v -> showDatePickerDialog(fromDateTimePicker));
        toDateTimePicker.setOnClickListener(v -> showDatePickerDialog(toDateTimePicker));
        searchButton.setOnClickListener(v -> filterCars());
        //Set sign-out button event
        signOutButton.setOnClickListener(v -> SignOutActivity.signOut(this, mAuth, googleSignInClient));

        //listen for fragment stack changes, refresh fragment UI
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            boolean hasFragments = getSupportFragmentManager().getBackStackEntryCount() > 0;
            recyclerViewCars.setVisibility(hasFragments ? View.GONE : View.VISIBLE);
            searchBar.setVisibility(hasFragments ? View.GONE : View.VISIBLE);
            fromDateTimePicker.setVisibility(hasFragments ? View.GONE : View.VISIBLE);
            toDateTimePicker.setVisibility(hasFragments ? View.GONE : View.VISIBLE);
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
                        //carList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Car car = document.toObject(Car.class);
                            car.setId(document.getId());  // Set the document ID as carId in the Car object
                            Log.d("CustomerDashboard", "---Car ID (raw): " + document.get("carId")); // Log seats as stored in Firestore

                            if (car != null && car.isAvailable()) {
                                searchedCars.add(car);
                               // carList.add(car.getImageUrls() + " " + car.getBrand() + " " + car.getModel() +
                                      //  " Number of seats: " + car.getSeats() + " - $" + car.getPrice());
                            }
                        }
                        //updateListView(carList);
                        carAdapter.notifyDataSetChanged();
                    } else {
                        Log.w("Firestore", "Error getting documents.", task.getException());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Failed to retrieve cars", e);
                });
    }

    private void filterCars() {
        String searchText = searchBar.getText().toString().toLowerCase();
        ArrayList<Car> filteredCars = new ArrayList<>();

        //carList.clear();

        for (Car car : searchedCars) {
            if (car.getBrand().toLowerCase().contains(searchText) ||
                    car.getModel().toLowerCase().contains(searchText)) {
                //carList.add(car.getImageUrls() + " " + car.getBrand() + " " + car.getModel() +
                      //  " Number of seats: " + car.getSeats() + " - $" + car.getPrice());
                filteredCars.add(car);
            }
        }
        //updateListView(carList);
        carAdapter = new CarAdapter(this, filteredCars);
        recyclerViewCars.setAdapter(carAdapter);
    }
//
//    private void updateListView(ArrayList<String> list) {
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(
//                this,
//                android.R.layout.simple_list_item_1,
//                list
//        );
//        listViewCars.setAdapter(adapter);
//    }

    private void showDatePickerDialog(EditText datePickerEditText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                CustomerDashboardActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> datePickerEditText.setText(String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)),
                year, month, day);

        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.show();



    }
}