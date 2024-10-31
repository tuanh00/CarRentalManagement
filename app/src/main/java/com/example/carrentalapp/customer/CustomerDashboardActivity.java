package com.example.carrentalapp.customer;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.carrentalapp.R;
import com.example.carrentalapp.auth.RegisterActivity;
import com.example.carrentalapp.common.Car;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CustomerDashboardActivity extends AppCompatActivity {

    private ListView listViewCars;
    private DatabaseReference database;
    private ArrayList<String> carList;
    private EditText fromDateTime, toDateTime, searchBar;
    private Spinner seatsSpinner, priceSpinner, brandSpinner, yearSpinner, modelSpinner;
    private Button searchButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_rental_app);

        listViewCars = findViewById(R.id.listViewCar);
        seatsSpinner = findViewById(R.id.seats);
        priceSpinner = findViewById(R.id.prices);
        brandSpinner = findViewById(R.id.brands);
        yearSpinner = findViewById(R.id.years);
        modelSpinner = findViewById(R.id.models);
        searchButton = findViewById(R.id.searchButton);
        fromDateTime = findViewById(R.id.fromDateTime);
        toDateTime = findViewById(R.id.toDateTime);
        searchBar = findViewById(R.id.searchBar);

        fromDateTime.setOnClickListener(v -> showDatePickerDialog());
        toDateTime.setOnClickListener(v -> showDatePickerDialog());



        database = FirebaseDatabase.getInstance().getReference("cars");
        carList = new ArrayList<>();


        loadCarData();

        searchButton.setOnClickListener(v -> {
            String searchedText = searchBar.getText().toString();
            searchCarFunction(searchedText);
        });
    }

    //Search Bar function
    private void searchCarFunction(String searchedCar) {
        List<String> searchedCarList = new ArrayList<>();
        for (String car : carList) {
            if (car.toLowerCase().contains(searchedCar.toLowerCase())) {
                searchedCarList.add(car);
            }
        }
        updateListView(searchedCarList);

        if (searchedCarList.isEmpty()) {
            Toast.makeText(this, "No cars match the search criteria.", Toast.LENGTH_SHORT).show();
        }
    }
    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                CustomerDashboardActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> fromDateTime.setText(String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)),
                year, month, day);
        datePickerDialog.show();
    }

    private void loadCarData() {
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                carList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Car car = postSnapshot.getValue(Car.class);
                    if (car != null) {
                        carList.add(car.getImageUrls() + car.getBrand() + " " + car.getModel() + " - C$: " + car.getPrice() + " - Seats: " + car.getSeats());
                    }
                }
                updateListView(carList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CustomerDashboardActivity.this, "Failed to load cars.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filtersApplied() {
        String selectedSeats = seatsSpinner.getSelectedItem().toString();
        String selectedPrice = priceSpinner.getSelectedItem().toString();
        String selectedBrand = brandSpinner.getSelectedItem().toString();
        String selectedYear = yearSpinner.getSelectedItem().toString();
        String selectedModel = modelSpinner.getSelectedItem().toString();

        List<String> filteredCarList = new ArrayList<>();
        for (String car : carList) {
            boolean matches = true;

            if (!selectedSeats.equals("Any") && !car.contains("Seats: " + selectedSeats)) {
                matches = false;
            }
            if (!selectedPrice.equals("Any") && !car.contains("C$: " + selectedPrice)) {
                matches = false;
            }
            if (!selectedBrand.equals("Any") && !car.contains(selectedBrand)) {
                matches = false;
            }
            if (!selectedYear.equals("Any") && !car.contains(selectedYear)) {
                matches = false;
            }
            if (!selectedModel.equals("Any") && !car.contains(selectedModel)) {
                matches = false;
            }

            if (matches) {
                filteredCarList.add(car);
            }
        }

        updateListView(filteredCarList);

        if (filteredCarList.isEmpty()) {
            Toast.makeText(this, "No cars match the search criteria.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateListView(List<String> carDisplayList) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, carDisplayList);
        listViewCars.setAdapter(adapter);
    }


}
