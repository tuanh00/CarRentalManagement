package com.example.carrentalapp.customer;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
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
import com.example.carrentalapp.common.CarAdapter;
import com.example.carrentalapp.common.ViewCarsFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.carrentalapp.common.Car;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CustomerDashboardActivity extends AppCompatActivity {

    private ListView listViewCars;

    private FirebaseFirestore database;
    private EditText searchBar, startDate_picker, returnDate_picker;
    private Button searchButton;
    final Calendar calendar = Calendar.getInstance();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_rental_app);


        searchButton = findViewById(R.id.searchButton);
        searchBar = findViewById(R.id.searchBar);
        startDate_picker = findViewById(R.id.fromDateTimePicker);
        returnDate_picker = findViewById(R.id.toDateTimePicker);

        database = FirebaseFirestore.getInstance();


        loadData();

        startDate_picker.setOnClickListener(v -> startingDatePicker());
        returnDate_picker.setOnClickListener(v -> returningDatePicker());

        searchButton.setOnClickListener(v -> {
            String searchedText = searchBar.getText().toString().trim();
            searchCarFunction(searchedText);
        });
    }

    private void searchCarFunction(String searchedCar) {
        carsRef.whereEqualTo("availability", true)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error fetching data.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<String> searchedCarList = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Car car = doc.toObject(Car.class);
                            String carInfo = car.getBrand() + " " + car.getModel() + " - C$: " + car.getPrice() + " - Seats: " + car.getSeats();

                            // Check if searched text matches brand or model
                            if (car.getBrand().toLowerCase().contains(searchedCar.toLowerCase()) ||
                                    car.getModel().toLowerCase().contains(searchedCar.toLowerCase())) {
                                searchedCarList.add(carInfo);
                            }
                        }
                    }
                    updateListView(searchedCarList);
                    if (searchedCarList.isEmpty()) {
                        Toast.makeText(this, "No cars match the search criteria.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Date picker for start date
    private void startingDatePicker(){
        DatePickerDialog.OnDateSetListener date = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            startDate_picker.setText(updateDate());
        };
        new DatePickerDialog(CustomerDashboardActivity.this, date, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    // Date picker for return date
    private void returningDatePicker(){
        DatePickerDialog.OnDateSetListener date = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            returnDate_picker.setText(updateDate());
        };
        new DatePickerDialog(CustomerDashboardActivity.this, date, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private String updateDate(){
        String dateFormat = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.CANADA);
        return simpleDateFormat.format(calendar.getTime());
    }
    private void loadData() {
        ViewCarsFragment fragment = new ViewCarsFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }



    private void updateListView(List<String> carDisplayList) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, carDisplayList);
        listViewCars.setAdapter(adapter);
    }
}


//private Spinner seatsSpinner, priceSpinner, brandSpinner, yearSpinner, modelSpinner;

//    private void showDatePickerDialog() {
//        final Calendar calendar = Calendar.getInstance();
//        int year = calendar.get(Calendar.YEAR);
//        int month = calendar.get(Calendar.MONTH);
//        int day = calendar.get(Calendar.DAY_OF_MONTH);
//
//        DatePickerDialog datePickerDialog = new DatePickerDialog(
//                CustomerDashboardActivity.this,
//                (view, selectedYear, selectedMonth, selectedDay) -> fromDateTime.setText(String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)),
//                year, month, day);
//        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
//        datePickerDialog.show();
//    }




//private void filtersApplied() {
//    String selectedSeats = seatsSpinner.getSelectedItem().toString();
//    String selectedPrice = priceSpinner.getSelectedItem().toString();
//    String selectedBrand = brandSpinner.getSelectedItem().toString();
//    String selectedYear = yearSpinner.getSelectedItem().toString();
//    String selectedModel = modelSpinner.getSelectedItem().toString();
//
//    List<String> filteredCarList = new ArrayList<>();
//    for (String car : carList) {
//        boolean matches = true;
//
//        if (!selectedSeats.equals("Any") && !car.contains("Seats: " + selectedSeats)) {
//            matches = false;
//        }
//        if (!selectedPrice.equals("Any") && !car.contains("C$: " + selectedPrice)) {
//            matches = false;
//        }
//        if (!selectedBrand.equals("Any") && !car.contains(selectedBrand)) {
//            matches = false;
//        }
//        if (!selectedYear.equals("Any") && !car.contains(selectedYear)) {
//            matches = false;
//        }
//        if (!selectedModel.equals("Any") && !car.contains(selectedModel)) {
//            matches = false;
//        }
//
//        if (matches) {
//            filteredCarList.add(car);
//        }
//    }
//
//    updateListView(filteredCarList);
//
//    if (filteredCarList.isEmpty()) {
//        Toast.makeText(this, "No cars match the search criteria.", Toast.LENGTH_SHORT).show();
//    }
//}
// Load all available cars
//    private void loadCarData() {
//        carsRef.whereEqualTo("availability", true)
//                .addSnapshotListener((value, error) -> {
//                    if (error != null) {
//                        Toast.makeText(this, "Failed to load cars.", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    carList.clear();
//                    if (value != null) {
//                        for (QueryDocumentSnapshot doc : value) {
//                            Car car = doc.toObject(Car.class);
//                            String carInfo = car.getBrand() + " " + car.getModel() + " - C$: " + car.getPrice() + " - Seats: " + car.getSeats();
//                            carList.add(carInfo);
//                        }
//                    }
//                    updateListView(carList);
//                });
//    }