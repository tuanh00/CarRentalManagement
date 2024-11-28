package com.example.carrentalapp.uiactivities.admin;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.carrentalapp.R;
import com.example.carrentalapp.adapters.CarAdapter;
import com.example.carrentalapp.models.Car;
import com.example.carrentalapp.states.car.CarAvailabilityState;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;


public class ViewCarsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextInputEditText searchBar;
    private CarAdapter carAdapter;
    private ArrayList<Car> carList;
    private FirebaseFirestore db;
    private static final String PREFS_NAME = "CarRentalAppPrefs";
    private static final String ROLE_KEY = "user_role";
    private ListenerRegistration carListener;

    public ViewCarsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_cars, container, false);

        db = FirebaseFirestore.getInstance();

        recyclerView = view.findViewById(R.id.recyclerViewCars);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        carList = new ArrayList<>();
        carAdapter = new CarAdapter(getContext(), carList, true);
        recyclerView.setAdapter(carAdapter);
        searchBar = view.findViewById(R.id.searchBar);

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCars(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed
            }
        });

        loadCars();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove the listener when the view is destroyed to prevent memory leaks
        if (carListener != null) {
            carListener.remove();
            carListener = null;
        }
    }

    private void filterCars(String searchText) {
        ArrayList<Car> filteredCars = new ArrayList<>();
        for (Car car : carList) {
            if (car.getBrand().toLowerCase().contains(searchText) ||
                    car.getModel().toLowerCase().contains(searchText) ||
                    String.valueOf(car.getSeats()).contains(searchText) ||
                    String.valueOf(car.getPrice()).contains(searchText) ||
                    String.valueOf(car.getRating()).contains(searchText)) {

                filteredCars.add(car);
            }
        }
        carAdapter.updateData(filteredCars);
    }

    private boolean isAdminUser() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return "admin".equals(sharedPreferences.getString(ROLE_KEY, "customer"));
    }

    /**
     * Load cars from the latest to the oldest and display based on user role.
     */
    private void loadCars() {
        // Remove existing listener if any
        if (carListener != null) {
            carListener.remove();
        }

        Query query = db.collection("Cars").orderBy("createdAt", Query.Direction.DESCENDING);

        carListener = query.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                Toast.makeText(getContext(), "Failed to load cars", Toast.LENGTH_SHORT).show();
                return;
            }

            if (queryDocumentSnapshots != null) {
                carList.clear();
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    Car car = document.toObject(Car.class);
                    car.setId(document.getId());
                    carList.add(car);
                }
                carAdapter.notifyDataSetChanged();
            }
        });
    }

}