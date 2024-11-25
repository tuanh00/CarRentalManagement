package com.example.carrentalapp.uiactivities.admin;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.carrentalapp.R;
import com.example.carrentalapp.adapters.CarAdapter;
import com.example.carrentalapp.models.Car;
import com.example.carrentalapp.states.car.CarAvailabilityState;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;


public class ViewCarsFragment extends Fragment {

    private RecyclerView recyclerView;
    private CarAdapter carAdapter;
    private ArrayList<Car> carList;
    private FirebaseFirestore db;
    private static final String PREFS_NAME = "CarRentalAppPrefs";
    private static final String ROLE_KEY = "user_role";
    private ListenerRegistration carListener; // Listen to the latest added car

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

        Query query = db.collection("Cars");

        if (!isAdminUser()) {
            // If not admin, only show available cars
            query = query.whereEqualTo("state", CarAvailabilityState.AVAILABLE.name());
        }

        // Order the cars by createdAt in descending order
        query = query.orderBy("createdAt", Query.Direction.DESCENDING);

        carListener = query.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                Toast.makeText(getContext(), "Failed to load cars", Toast.LENGTH_SHORT).show();
                Log.e("FirebaseError", "Error loading cars", e);
                return;
            }

            if (queryDocumentSnapshots != null) {
                carList.clear();
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    Car car = document.toObject(Car.class);
                    car.setId(document.getId());

                    // Retrieve and set the availability state based on the stored value
                    String state = document.getString("state");
                    if (state != null) {
                        car.setCurrentState(CarAvailabilityState.valueOf(state.toUpperCase()));
                    } else {
                        car.setCurrentState(CarAvailabilityState.UNAVAILABLE); // Default if not set
                    }

                    carList.add(car);
                }
                carAdapter.notifyDataSetChanged();
            }
        });
    }

}