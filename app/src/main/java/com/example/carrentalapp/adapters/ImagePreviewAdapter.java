package com.example.carrentalapp.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.carrentalapp.R;

import java.util.ArrayList;

public class ImagePreviewAdapter extends RecyclerView.Adapter<ImagePreviewAdapter.ImageViewHolder> {
    private ArrayList<Uri> imageUris;
    private Context context;
    private OnImageRemoveListener onImageRemoveListener;

    public interface OnImageRemoveListener {
        void onRemove(int position);
    }

    public ImagePreviewAdapter(Context context, ArrayList<Uri> imageUris, OnImageRemoveListener listener) {
        this.context = context;
        this.imageUris = imageUris != null ? imageUris : new ArrayList<>(); // Ensure non-null list
        this.onImageRemoveListener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image_preview, parent, false);
        return new ImageViewHolder(view, onImageRemoveListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        if (imageUris.isEmpty()) {
            // Show a placeholder if there are no images
            Glide.with(context)
                    .load(R.drawable.car_placeholder) // Set your placeholder drawable resource
                    .into(holder.imageView);
            holder.removeButton.setVisibility(View.GONE); // Hide remove button when there's no image
        } else {
            Uri imageUri = imageUris.get(position);
            Glide.with(context)
                    .load(imageUri)
                    .placeholder(R.drawable.car_placeholder)
                    .into(holder.imageView);
            holder.removeButton.setVisibility(View.VISIBLE); // Show remove button for actual images
        }
    }

    @Override
    public int getItemCount() {
        return imageUris.isEmpty() ? 1 : imageUris.size(); // Return 1 to show placeholder if empty
    }

    public void updateImages(ArrayList<Uri> newImageUris) {
        imageUris = newImageUris != null ? newImageUris : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton removeButton;

        public ImageViewHolder(@NonNull View itemView, OnImageRemoveListener listener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imagePreview);
            removeButton = itemView.findViewById(R.id.buttonRemoveImage);

            removeButton.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onRemove(position);
                    }
                }
            });
        }
    }
}