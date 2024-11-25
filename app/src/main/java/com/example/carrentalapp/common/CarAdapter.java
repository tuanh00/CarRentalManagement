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
import com.example.carrentalapp.models.Contract;
import com.example.carrentalapp.states.car.CarAvailabilityState;
import com.example.carrentalapp.uiactivities.admin.EditCarFragment;
import com.example.carrentalapp.models.Car;
import com.example.carrentalapp.uiactivities.customer.RentCarFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {

    private List<Car> carList;
    private Context context;
    private boolean isAdmin;

    public CarAdapter(Context context, List<Car> carList, boolean isAdmin) {
        this.context = context;
        this.carList = carList;
        this.isAdmin = isAdmin;
    }


    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_car, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        // Get the current car
        Car car = carList.get(position);

        // Set data to the views
        holder.carBrandModel.setText(car.getBrand() + " " + car.getModel());
        holder.carPrice.setText("" + car.getPrice());
        holder.carSeats.setText(String.valueOf(car.getSeats()));
        holder.carLocation.setText(car.getLocation());
        holder.carRating.setRating(car.getRating());
        holder.ratingCount.setText(car.getRatingCount() + " ratings");

        // Load car images
        if (car.getImages() != null && !car.getImages().isEmpty()) {
            Glide.with(context)
                    .load(car.getImages().get(0))
                    .placeholder(R.drawable.car_placeholder)
                    .into(holder.carImage);
        } else {
            holder.carImage.setImageResource(R.drawable.car_placeholder);
        }

        // Set up the action button based on user role
        if (isAdmin) {
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

        // Handle click events on the action button
        holder.actionButton.setOnClickListener(v -> {
            if (isAdmin) {
                FragmentActivity fragmentActivity = (FragmentActivity) context;
                EditCarFragment editCarFragment = new EditCarFragment();
                Bundle bundleEdit = new Bundle();
                bundleEdit.putString("carId", car.getId());
                bundleEdit.putString("carBrand", car.getBrand());
                bundleEdit.putString("carModel", car.getModel());
                bundleEdit.putString("carLocation", car.getLocation());
                bundleEdit.putInt("carSeats", car.getSeats());
                bundleEdit.putDouble("carPrice", car.getPrice());
                bundleEdit.putStringArrayList("carImageUrls", new ArrayList<>(car.getImages()));
                bundleEdit.putString("state", car.getCurrentState().toString());

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
    public void updateData(List<Car> newCarList) {
        this.carList = newCarList;
        notifyDataSetChanged();
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