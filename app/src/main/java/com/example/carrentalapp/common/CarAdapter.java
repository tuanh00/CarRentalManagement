package com.example.carrentalapp.common;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.carrentalapp.R;
import com.example.carrentalapp.admin.EditCarFragment;

import java.util.List;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {

    private List<Car> carList;
    private Context context;
    private static final String PREFS_NAME = "CarRentalAppPrefs";
    private static final String ROLE_KEY = "user_role";

    public CarAdapter(Context context, List<Car> carList) {
        this.context = context;
        this.carList = carList;
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

        // Load car image
        if (car.getImageUrls() != null && !car.getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(car.getImageUrls().get(0))
                    .placeholder(R.drawable.car_placeholder)
                    .into(holder.carImage);
        } else {
            holder.carImage.setImageResource(R.drawable.car_placeholder);
        }

        // Display appropriate action for admin and customer
        holder.actionButton.setText(isAdminUser() ? "Edit" : "Rent");
        holder.actionButton.setOnClickListener(v -> {
            if (isAdminUser()) {
                Intent intent = new Intent(context, EditCarFragment.class);
                intent.putExtra("carId", car.getId());
                context.startActivity(intent);
            } else {
                FragmentActivity fragmentActivity = (FragmentActivity) context;
                RentCarFragment rentCarFragment = new RentCarFragment();

                // Pass car details to RentCarFragment
                Bundle bundle = new Bundle();
                bundle.putString("carId", car.getId()); // Document ID as carId
                bundle.putString("carBrandModel", car.getBrand() + " " + car.getModel());
                bundle.putString("carLocation", car.getLocation());
                bundle.putInt("carSeats", car.getSeats());
                bundle.putDouble("carPrice", car.getPrice());
                bundle.putFloat("carRating", car.getRating());
                if (car.getImageUrls() != null && !car.getImageUrls().isEmpty()) {
                    bundle.putString("carImageUrl", car.getImageUrls().get(0));
                }
                rentCarFragment.setArguments(bundle);

                fragmentActivity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.customerFragmentContainer, rentCarFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return carList.size();
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
