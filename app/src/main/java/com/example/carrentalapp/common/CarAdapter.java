package com.example.carrentalapp.common;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.carrentalapp.R;
import com.example.carrentalapp.common.Car;

import java.util.List;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {

    private List<Car> carList;
    private Context context;

    public CarAdapter(Context context, List<Car> carList) {
        this.context = context;
        this.carList = carList;
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
            holder.carLocation.setText(car.getLocation());
            holder.carPrice.setText(String.valueOf(car.getPrice()));
            holder.carSeats.setText(String.valueOf(car.getSeats()));
            holder.carRating.setRating(car.getRating()); //float ?
            holder.ratingCount.setText(String.valueOf(car.getRatingCount()));

            //load the first image from the list of image URLs (if any)
        if(car.getImageUrls() != null && !car.getImageUrls().isEmpty()) {
            Glide.with(context)
                    .load(car.getImageUrls().get(0))
                    .placeholder(R.drawable.car_placeholder) // Placeholder image until loading completes
                    .into(holder.carImage);
        }
        else {
            holder.carImage.setImageResource(R.drawable.car_placeholder); // Default placeholder
        }

    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    public static class CarViewHolder extends RecyclerView.ViewHolder {
        TextView carBrandModel, carLocation, carPrice, carSeats, ratingCount;
        ImageView carImage;
        RatingBar carRating;


        public CarViewHolder(@NonNull View itemView) {
            super(itemView);
            carBrandModel = itemView.findViewById(R.id.carBrandModel);
            carLocation = itemView.findViewById(R.id.carLocation);
            carPrice = itemView.findViewById(R.id.carPrice);
            carSeats = itemView.findViewById(R.id.carSeats);
            carImage = itemView.findViewById(R.id.carImage);
            carRating = itemView.findViewById(R.id.carRating);
            ratingCount = itemView.findViewById(R.id.ratingCount);
        }
    }
}
