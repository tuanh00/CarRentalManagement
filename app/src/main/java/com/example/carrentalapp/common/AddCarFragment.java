package com.example.carrentalapp.common;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carrentalapp.R;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddCarFragment extends Fragment {

    /* this.model = model;
        this.brand = brand;
        this.seats = seats;
        this.location = location;
        this.imageUrl = imageUrl;
        this.price = price;*/
    private EditText inputCarModel, inputCarBrand, inputCarSeats, inputCarLocation, inputCarPrice;
    private ArrayList<Uri> imageUris;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Button  buttonSelectImages, buttonSubmitCarDetails;
    private RecyclerView imageRecyclerView;
    private ImagePreviewAdapter imagePreviewAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_car, container, false);

        inputCarModel = view.findViewById(R.id.inputCarModel);
        inputCarBrand = view.findViewById(R.id.inputCarBrand);
        inputCarSeats = view.findViewById(R.id.inputCarSeats);
        inputCarLocation = view.findViewById(R.id.inputCarLocation);
        inputCarPrice = view.findViewById(R.id.inputCarPrice);
        buttonSelectImages = view.findViewById(R.id.buttonSelectImages);
        buttonSubmitCarDetails = view.findViewById(R.id.buttonSubmitCarDetails);
        imageRecyclerView = view.findViewById(R.id.imageRecyclerView);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        imageUris = new ArrayList<>();
        imagePreviewAdapter = new ImagePreviewAdapter(getContext(), imageUris);
        imageRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        imageRecyclerView.setAdapter(imagePreviewAdapter);

        buttonSelectImages.setOnClickListener(v -> selectImages());
        buttonSubmitCarDetails.setOnClickListener(v -> uploadCarDetails());

        return view;
    }

    private void selectImages() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == getActivity().RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    imageUris.add(data.getClipData().getItemAt(i).getUri());
                }
            } else if (data.getData() != null) {
                imageUris.add(data.getData());
            }
            imagePreviewAdapter.notifyDataSetChanged();
        }
    }

    private void uploadCarDetails() {
        String model = inputCarModel.getText().toString().trim();
        String brand = inputCarBrand.getText().toString().trim();
        String seats = inputCarSeats.getText().toString().trim();
        String price = inputCarPrice.getText().toString().trim();
        String location = inputCarLocation.getText().toString().trim();

        if (TextUtils.isEmpty(model) || TextUtils.isEmpty(brand) || TextUtils.isEmpty(seats) || TextUtils.isEmpty(price) || TextUtils.isEmpty(location)) {
            Toast.makeText(getContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Code to save car details in database (Firebase Firestore or local storage)
        ArrayList<String> imageUrls = new ArrayList<>();
        for (Uri uri : imageUris) {
            String fileName = UUID.randomUUID().toString();
            StorageReference storageRef = storage.getReference().child("cars/" + fileName);
            storageRef.putFile(uri).addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri1 -> {
                imageUrls.add(uri1.toString());
                if(imageUrls.size() == imageUris.size()) {
                    saveCarToDatabase(model, brand, Integer.parseInt(seats), Double.parseDouble(price), location, imageUrls);
                }
            }));

        }

    }
    private void saveCarToDatabase(String model, String brand, int seats, double price, String location, ArrayList<String> imageUrls) {
        Map<String, Object> carData = new HashMap<>();
        carData.put("model", model);
        carData.put("brand", brand);
        carData.put("seats", seats);
        carData.put("price", price);
        carData.put("location", location);
        carData.put("images", imageUrls);
        carData.put("rating", 0); //new car by default have no rating
        carData.put("ratingCount", 0);
        carData.put("createdAt", FieldValue.serverTimestamp());  // Server timestamp -> filter to display the latest to oldest
        carData.put("isAvailable", true);

        db.collection("Cars").add(carData).addOnSuccessListener(documentReference -> {
            String carId = documentReference.getId();  // Capture the auto-generated ID
            Toast.makeText(getContext(), "Car added successfully", Toast.LENGTH_SHORT).show();

            // Clear all fields
            inputCarModel.setText("");
            inputCarBrand.setText("");
            inputCarSeats.setText("");
            inputCarLocation.setText("");
            inputCarPrice.setText("");
            imageUris.clear(); // Clear image list
            imagePreviewAdapter.notifyDataSetChanged(); // Update the RecyclerView to reflect cleared images

        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to add car", Toast.LENGTH_SHORT).show();
        });
    }


}
