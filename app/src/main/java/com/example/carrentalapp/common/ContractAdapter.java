package com.example.carrentalapp.common;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.carrentalapp.R;
import com.example.carrentalapp.admin.EditCarFragment;

import java.util.List;

public class ContractAdapter extends RecyclerView.Adapter<ContractAdapter.ContractViewHolder> {

    private List<Car> carList;
    private Context context;
    private boolean isAdminUser;

    public ContractAdapter(Context context, List<Car> carList, boolean isAdminUser) {
        this.context = context;
        this.carList = carList;
        this.isAdminUser = isAdminUser;
    }

    @NonNull
    @Override
    public ContractViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_car, parent, false);
        return new ContractViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContractViewHolder holder, int position) {
        Car car = carList.get(position);
        holder.carBrandModel.setText(car.getBrand() + " " + car.getModel());
        holder.carPrice.setText("$" + car.getPrice());
        holder.carSeats.setText(car.getSeats() + " seats");
        holder.carRating.setRating(car.getRating());

        Glide.with(context)
                .load(car.getImageUrls().get(0))
                .placeholder(R.drawable.car_placeholder)
                .into(holder.carImage);

        holder.actionButton.setText(isAdminUser ? "Edit" : "Rent");
        holder.actionButton.setOnClickListener(v -> {
            if (isAdminUser) {
                Intent intent = new Intent(context, EditCarFragment.class);
                intent.putExtra("carId", car.getId());
                context.startActivity(intent);
            } else {
                Intent intent = new Intent(context, RentCarFragment.class);
                intent.putExtra("carId", car.getId());
                intent.putExtra("carName", car.getBrand() + " " + car.getModel());
                intent.putExtra("pricePerDay", car.getPrice());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    public static class ContractViewHolder extends RecyclerView.ViewHolder {
        TextView carBrandModel, carPrice, carSeats;
        ImageView carImage;
        RatingBar carRating;
        Button actionButton;

        public ContractViewHolder(@NonNull View itemView) {
            super(itemView);
            carBrandModel = itemView.findViewById(R.id.carBrandModel);
            carPrice = itemView.findViewById(R.id.carPrice);
            carSeats = itemView.findViewById(R.id.carSeats);
            carImage = itemView.findViewById(R.id.carImage);
            carRating = itemView.findViewById(R.id.carRating);
            actionButton = itemView.findViewById(R.id.actionButton);
        }
    }
}
