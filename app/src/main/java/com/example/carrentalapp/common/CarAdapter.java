package com.example.carrentalapp.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.carrentalapp.R;
import com.example.carrentalapp.states.car.CarAvailabilityState;
import com.example.carrentalapp.uiactivities.admin.EditCarFragment;
import com.example.carrentalapp.models.Car;
import com.example.carrentalapp.uiactivities.customer.RentCarFragment;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {

    private List<Car> carList;
    private Context context;
    private static final String PREFS_NAME = "CarRentalAppPrefs";
    private static final String ROLE_KEY = "user_role";
    private FirebaseFirestore db;

    public CarAdapter(Context context, List<Car> carList) {
        this.context = context;
        this.carList = carList;
        this.db = FirebaseFirestore.getInstance();
    }
    private boolean isAdminUser() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return "admin".equals(sharedPreferences.getString(ROLE_KEY, "customer"));
    }
    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_car, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        Car car = carList.get(position);
        holder.carBrandModel.setText(car.getBrand() + " " + car.getModel());
        holder.carPrice.setText("Price per day: $" + car.getPrice()); // Ensure it shows correctly
        holder.carSeats.setText(String.valueOf(car.getSeats()));
        holder.carLocation.setText(car.getLocation());
        holder.carRating.setRating(car.getRating());
        holder.ratingCount.setText(car.getRatingCount() + " ratings"); // Show rating count as "x ratings"

        // Load car images into the ImageView (displaying the first image as a preview)
        if (car.getImages() != null && !car.getImages().isEmpty()) {
            Glide.with(context)
                    .load(car.getImages().get(0))
                    .placeholder(R.drawable.car_placeholder)
                    .into(holder.carImage);
        } else {
            holder.carImage.setImageResource(R.drawable.car_placeholder);
        }

        // Display appropriate action for admin and customer
        if (isAdminUser()) {
            holder.actionButton.setEnabled(true);
            holder.actionButton.setText("Edit");
        } else {
            if (car.getCurrentState() == CarAvailabilityState.AVAILABLE) {
                holder.actionButton.setEnabled(true);
                holder.actionButton.setText("Rent");
            } else {
                holder.actionButton.setEnabled(false);
                holder.actionButton.setText("Unavailable");
            }
        }
        // Set button click listener
        holder.actionButton.setOnClickListener(v -> {
            if (isAdminUser()) {
                FragmentActivity fragmentActivity = (FragmentActivity) context;
                EditCarFragment editCarFragment = new EditCarFragment();

                //pass car details to EditCarFragment
                Bundle bundleEdit = new Bundle();
                bundleEdit.putString("carId", car.getId());
                bundleEdit.putString("carBrand", car.getBrand());
                bundleEdit.putString("carModel", car.getModel());
                bundleEdit.putString("carLocation", car.getLocation());
                bundleEdit.putInt("carSeats", car.getSeats());
                bundleEdit.putDouble("carPrice", car.getPrice());
                bundleEdit.putStringArrayList("carImageUrls", new ArrayList<>(car.getImages()));
                bundleEdit.putString("availabilityState", car.getCurrentState().name());

                editCarFragment.setArguments(bundleEdit);

                fragmentActivity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.adminFragmentContainer, editCarFragment)
                        .addToBackStack(null)
                        .commit();
            } else {
                if(car.getCurrentState() == CarAvailabilityState.AVAILABLE) {
                    FragmentActivity fragmentActivity = (FragmentActivity) context;
                    RentCarFragment rentCarFragment = new RentCarFragment();

                    //pass car details to RentCarFragment
                    Bundle bundle = new Bundle();
                    bundle.putString("carId", car.getId());
                    bundle.putString("carBrandModel", car.getBrand() + " " + car.getModel());
                    bundle.putString("carLocation", car.getLocation());
                    bundle.putInt("carSeats", car.getSeats());
                    bundle.putDouble("carPrice", car.getPrice());
                    bundle.putFloat("carRating", car.getRating());
                    bundle.putStringArrayList("carImageUrls", new ArrayList<>(car.getImages()));

                    rentCarFragment.setArguments(bundle);

                    fragmentActivity.getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.customerFragmentContainer, rentCarFragment)
                            .addToBackStack(null)
                            .commit();
                } else {
                    Toast.makeText(context, "Car is currently unavailable", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }


    public void updateCarDetails(String carId, String newBrand, String newModel, int newSeats, double newPrice, ArrayList<String> newImages) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a map with the updated fields
        Map<String, Object> updates = new HashMap<>();
        updates.put("brand", newBrand);
        updates.put("model", newModel);
        updates.put("seats", newSeats);
        updates.put("price", newPrice);
        updates.put("images", newImages);

        // Update the car in Firestore
        db.collection("Cars").document(carId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Car updated successfully", Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged(); // Refresh adapter data if necessary
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to update car: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public static class CarViewHolder extends RecyclerView.ViewHolder {
        TextView carBrandModel, carLocation, carPrice, carSeats, ratingCount;
        ImageView carImage;
        RatingBar carRating;
        Button actionButton;

        public CarViewHolder(@NonNull View itemView) {
            super(itemView);
            carBrandModel = itemView.findViewById(R.id.carBrandModel);
            carLocation = itemView.findViewById(R.id.carLocation);
            carPrice = itemView.findViewById(R.id.carPrice);
            carSeats = itemView.findViewById(R.id.carSeats);
            carImage = itemView.findViewById(R.id.carImage);
            carRating = itemView.findViewById(R.id.carRating);
            ratingCount = itemView.findViewById(R.id.ratingCount);
            actionButton = itemView.findViewById(R.id.actionButton);
        }
    }
}
