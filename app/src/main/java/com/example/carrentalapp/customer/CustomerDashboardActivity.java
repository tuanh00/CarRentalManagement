package com.example.carrentalapp.customer;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    ListView listViewCars;
    EditText searchBar, fromDateTimePicker, toDateTimePicker;
    Button searchButton, signOutButton;
    FirebaseFirestore database;
    private CarAdapter carAdapter;

    ArrayList<String> carList;
    ArrayList<Car> searchedCars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_rental_app);

        listViewCars = findViewById(R.id.listViewCars);
        searchBar = findViewById(R.id.searchBar);
        fromDateTimePicker = findViewById(R.id.fromDateTimePicker);
        toDateTimePicker = findViewById(R.id.toDateTimePicker);
        searchButton = findViewById(R.id.searchButton);
        signOutButton = findViewById(R.id.signOutButton);

        database = FirebaseFirestore.getInstance();
        carList = new ArrayList<>();
        searchedCars = new ArrayList<>();

        loadCarDatas();

        // Set up date pickers
        fromDateTimePicker.setOnClickListener(v -> showDatePickerDialog(fromDateTimePicker));
        toDateTimePicker.setOnClickListener(v -> showDatePickerDialog(toDateTimePicker));

        // Set up search button click listener
        searchButton.setOnClickListener(v -> filterCars());
    }

    private void loadCarDatas() {
        database.collection("Cars").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        searchedCars.clear();
                        carList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Car car = document.toObject(Car.class);
                            if (car != null && car.isAvailability()) {
                                searchedCars.add(car);
                                carList.add(car.getImageUrls() + " " + car.getBrand() + " " + car.getModel() +
                                        " Number of seats: " + car.getSeats() + " - $" + car.getPrice());
                            }
                        }
                        updateListView(carList);
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

        carList.clear();

        for (Car car : searchedCars) {
            if (car.getBrand().toLowerCase().contains(searchText) ||
                    car.getModel().toLowerCase().contains(searchText)) {
                carList.add(car.getImageUrls() + " " + car.getBrand() + " " + car.getModel() +
                        " Number of seats: " + car.getSeats() + " - $" + car.getPrice());
            }
        }
        updateListView(carList);
    }

    private void updateListView(ArrayList<String> list) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                list
        );
        listViewCars.setAdapter(adapter);
    }

    private void showDatePickerDialog(EditText datePickerEditText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                CustomerDashboardActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> datePickerEditText.setText(String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)),
                year, month, day);
        datePickerDialog.show();
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