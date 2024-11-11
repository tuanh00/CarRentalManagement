package com.example.carrentalapp.common;

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
import com.example.carrentalapp.models.Car;
import com.example.carrentalapp.states.car.CarAvailabilityState;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;


public class ViewCarsFragment extends Fragment {


    private RecyclerView recyclerView;
    private CarAdapter carAdapter;
    private ArrayList<Car> carList;
    private FirebaseFirestore db;
    private String carId;
    private static final String PREFS_NAME = "CarRentalAppPrefs";
    private static final String ROLE_KEY = "user_role";

    public ViewCarsFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_cars, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewCars);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        carList = new ArrayList<>();
        carAdapter = new CarAdapter(getContext(), carList);
        recyclerView.setAdapter(carAdapter);
        db = FirebaseFirestore.getInstance();

        loadCars();

        return view;
    }

    private boolean isAdminUser() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return "admin".equals(sharedPreferences.getString(ROLE_KEY, "customer"));
    }

    /**
     * Load cars from Firestore based on user role.
     */
    private void loadCars() {
        db.collection("Cars")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    carList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Car car = document.toObject(Car.class);
                        car.setId(document.getId());

                        // Retrieve and set the availability state based on the stored value
                        String state = document.getString("state");
                        if (state != null) {
                            car.setCurrentState(CarAvailabilityState.valueOf(state.toUpperCase()));
                        } else {
                            car.setCurrentState(CarAvailabilityState.UNAVAILABLE); // Default if not set
                        }

                        if (isAdminUser() || car.getCurrentState() ==  CarAvailabilityState.AVAILABLE) {
                            carList.add(car);
                        }
                    }
                    carAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load cars", Toast.LENGTH_SHORT).show();
                    Log.e("FirebaseError", "Error loading cars", e);
                });
    }


}