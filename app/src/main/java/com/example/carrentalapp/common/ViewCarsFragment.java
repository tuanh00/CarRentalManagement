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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;


public class ViewCarsFragment extends Fragment {


    private RecyclerView recyclerView;
    private CarAdapter carAdapter;
    private ArrayList<Car> carList;
    private FirebaseFirestore db;

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

        //loadCars() base on role
        loadCars();

        return view;
    }

    private boolean isAdminUser() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("CarRentalAppPrefs", Context.MODE_PRIVATE);
        return "admin".equals(sharedPreferences.getString("user_role", "customer"));
    }

    //admin -> view all cars / customer -> only view available cars
    private void loadCars(){
        db.collection("Cars").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    carList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Car car = document.toObject(Car.class);
                        if (isAdminUser() || car.isAvailable()) {  // Admins see all, customers see available only
                            carList.add(car);
                        }
                    }
                    carAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.d("ViewCarsFragment.java failed load cars", e.getMessage());
                });
    }


}