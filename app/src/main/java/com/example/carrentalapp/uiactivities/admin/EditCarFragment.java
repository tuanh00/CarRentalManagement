package com.example.carrentalapp.uiactivities.admin;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.appcompat.widget.SwitchCompat;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.carrentalapp.BuildConfig;
import com.example.carrentalapp.R;
import com.example.carrentalapp.adapters.ImagePreviewAdapter;
import com.example.carrentalapp.states.car.CarAvailabilityState;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class EditCarFragment extends Fragment {

    private EditText brandEditText, modelEditText, seatsEditText, priceEditText, locationEditText, descriptionEditText;
    private RatingBar ratingBar;
    private TextView ratingCountTextView;
    private SwitchCompat availabilitySwitch;
    private ImageView carImageView;
    private Button buttonEditImages, buttonSaveCarDetails;
    private RecyclerView imageRecyclerView;
    private ImagePreviewAdapter imagePreviewAdapter;
    private ArrayList<Uri> imageUris;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String carId;
    private ArrayList<String> imagesToDelete;
    private static final int PICK_IMAGES_REQUEST_CODE = 103;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 100;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        imageUris = new ArrayList<>();
        imagesToDelete = new ArrayList<>();

        // Initialize Google Places API
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), BuildConfig.GOOGLE_MAPS_API_KEY);
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_car, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind views
        brandEditText = view.findViewById(R.id.brandEditText);
        modelEditText = view.findViewById(R.id.modelEditText);
        descriptionEditText = view.findViewById(R.id.descriptionEditText);
        seatsEditText = view.findViewById(R.id.seatsEditText);
        priceEditText = view.findViewById(R.id.priceEditText);
        locationEditText = view.findViewById(R.id.locationEditText);
        availabilitySwitch = view.findViewById(R.id.availabilitySwitch);
        carImageView = view.findViewById(R.id.carImage);
        buttonEditImages = view.findViewById(R.id.buttonEditImage);
        buttonSaveCarDetails = view.findViewById(R.id.saveButton);
        imageRecyclerView = view.findViewById(R.id.imageRecyclerView);
        ratingBar = view.findViewById(R.id.carRatingBar);
        ratingCountTextView = view.findViewById(R.id.textViewRatingCount);

        // Initialize image list and adapter
        imagePreviewAdapter = new ImagePreviewAdapter(getContext(), imageUris, position -> confirmImageRemoval(position));
        imageRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        imageRecyclerView.setAdapter(imagePreviewAdapter);

        // Set button listeners
        buttonEditImages.setOnClickListener(v -> selectImages());
        buttonSaveCarDetails.setOnClickListener(v -> saveCarDetails());

        locationEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(requireContext());
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
            }
        });

        // Retrieve car data from arguments
        Bundle bundle = getArguments();
        if (bundle != null) {
            carId = bundle.getString("carId", "");
            String carBrand = bundle.getString("carBrand", "");
            String carModel = bundle.getString("carModel", "");
            String carDescription = bundle.getString("carDescription","");
            int carSeats = bundle.getInt("carSeats", 4);
            double carPrice = bundle.getDouble("carPrice", 0.0);
            String carLocation = bundle.getString("carLocation", "");
            CarAvailabilityState availabilityState = CarAvailabilityState.valueOf(bundle.getString("state", CarAvailabilityState.AVAILABLE.toString()));
            float carRating = bundle.getFloat("rating", 0.0f);
            int carRatingCount = bundle.getInt("ratingCount", 0);

            // Populate UI elements with car data
            brandEditText.setText(carBrand);
            modelEditText.setText(carModel);
            descriptionEditText.setText(carDescription);
            seatsEditText.setText(String.valueOf(carSeats));
            priceEditText.setText(String.valueOf(carPrice));
            locationEditText.setText(carLocation);
            availabilitySwitch.setChecked(availabilityState == CarAvailabilityState.AVAILABLE);
            ratingBar.setRating(carRating);
            ratingCountTextView.setText(Integer.toString(carRatingCount) + " ratings");
            // Load existing images
            ArrayList<String> existingImageUrls = bundle.getStringArrayList("carImageUrls");
            if (existingImageUrls != null && !existingImageUrls.isEmpty()) {
                for (String url : existingImageUrls) {
                    imageUris.add(Uri.parse(url));
                }
                imagePreviewAdapter.notifyDataSetChanged();
                // Load the first image into ImageView as a preview
                Glide.with(this).load(existingImageUrls.get(0)).into(carImageView);
            } else {
                // Load placeholder image if no images exist
                carImageView.setImageResource(R.drawable.car_placeholder);
            }
        } else {
            Toast.makeText(getContext(), "Failed to load car details", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Allows admin to select multiple images from the device.
     */
    private void selectImages() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), PICK_IMAGES_REQUEST_CODE);
    }

    /**
     * Handles the result from image selection.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            locationEditText.setText(place.getName());
        }

        // Handle Image Selection result
        if (requestCode == PICK_IMAGES_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) { // multiple images selected
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    imageUris.add(data.getClipData().getItemAt(i).getUri());
                }
            } else if (data.getData() != null) { // single image selected
                imageUris.add(data.getData());
            }
            imagePreviewAdapter.notifyDataSetChanged();

            // Update ImageView preview with the first image
            if (!imageUris.isEmpty()) {
                Glide.with(this).load(imageUris.get(0)).into(carImageView);
            } else {
                carImageView.setImageResource(R.drawable.car_placeholder);
            }
        }
    }

    /**
     * Confirm image removal with the admin.
     *
     * @param position The position of the image to remove.
     */
    private void confirmImageRemoval(int position) {
        new AlertDialog.Builder(getContext())
                .setTitle("Remove Image")
                .setMessage("Are you sure you want to remove this image?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    String imageUrl = imageUris.get(position).toString();
                    if (imageUrl.startsWith("https://firebasestorage.googleapis.com/")) {
                        // Convert full URL to path format if necessary
                        String imagePath = getImagePathFromUrl(imageUrl);
                        if (imagePath != null) {
                            imagesToDelete.add(imagePath);
                        }
                    }
                    imageUris.remove(position);
                    imagePreviewAdapter.notifyDataSetChanged();

                    // Update ImageView preview
                    if (!imageUris.isEmpty()) {
                        Glide.with(this).load(imageUris.get(0)).into(carImageView);
                    } else {
                        carImageView.setImageResource(R.drawable.car_placeholder);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Uploads new images and updates car details in Firestore.
     */
    private void saveCarDetails() {
        String brand = brandEditText.getText().toString().trim();
        String model = modelEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String seatsStr = seatsEditText.getText().toString().trim();
        String priceStr = priceEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(brand) || TextUtils.isEmpty(model) || TextUtils.isEmpty(seatsStr) ||
                TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(location)) {
            Toast.makeText(getContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int seats;
        double price;
        try {
            seats = Integer.parseInt(seatsStr);
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Seats must be an integer and Price must be a number", Toast.LENGTH_SHORT).show();
            return;
        }

        CarAvailabilityState availabilityState = availabilitySwitch.isChecked() ? CarAvailabilityState.AVAILABLE : CarAvailabilityState.UNAVAILABLE;

        // Start uploading images (if any)
        if (!imageUris.isEmpty()) {
            uploadImagesAndUpdateCar(brand, model, seats, price, location, availabilityState, description);
        } else {
            // If no images, proceed to update car details with empty imageUrls
            updateCarInFirestore(brand, model, seats, price, location, availabilityState, new ArrayList<>(), description);
        }
    }


    /**
     * Uploads selected images to Firebase Storage and retrieves their URLs.
     */
    private void uploadImagesAndUpdateCar(String brand, String model, int seats, double price,
                                          String location, CarAvailabilityState isAvailable, String description) {
        ArrayList<String> uploadedImageUrls = new ArrayList<>();
        final int totalImages = imageUris.size();
        final int[] uploadedCount = {0};

        for (Uri uri : imageUris) {
            String imageUrl = uri.toString();
            if (imageUrl.startsWith("https://firebasestorage.googleapis.com/")) {
                uploadedImageUrls.add(imageUrl);
                uploadedCount[0]++;
                if (uploadedCount[0] == totalImages) {
                    updateCarInFirestore(brand, model, seats, price, location, isAvailable, uploadedImageUrls, description);
                }
            } else {
                String fileName = UUID.randomUUID().toString();
                StorageReference storageRef = storage.getReference().child("cars/" + carId + "/" + fileName);
                storageRef.putFile(uri)
                        .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                                .addOnSuccessListener(downloadUri -> {
                                    uploadedImageUrls.add(downloadUri.toString());
                                    uploadedCount[0]++;
                                    if (uploadedCount[0] == totalImages) {
                                        updateCarInFirestore(brand, model, seats, price, location, isAvailable, uploadedImageUrls, description);
                                    }
                                }))
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to upload images: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        }
    }

    /**
     * Updates the car details in Firestore, including adding and removing images.
     */
    private void updateCarInFirestore(String brand, String model, int seats, double price,
                                      String location, CarAvailabilityState availabilityState, ArrayList<String> imageUrls, String description) {
        Map<String, Object> carData = new HashMap<>();
        carData.put("brand", brand);
        carData.put("model", model);
        carData.put("description", description);
        carData.put("seats", seats);
        carData.put("price", price);
        carData.put("location", location);
        carData.put("state", availabilityState.name());
        carData.put("images", imageUrls);
        carData.put("updatedAt", FieldValue.serverTimestamp());

        // Retain existing rating and ratingCount
        db.collection("Cars").document(carId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        double rating = documentSnapshot.getDouble("rating");
                        long ratingCount = documentSnapshot.getLong("ratingCount");

                        carData.put("rating",rating);
                        carData.put("ratingCount", ratingCount);

                        // Update Firestore
                        db.collection("Cars").document(carId).update(carData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Car details updated successfully", Toast.LENGTH_SHORT).show();
                                    navigateToAdminDashboard();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Failed to update car: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error retrieving car details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Extracts the storage path from a Firebase Storage URL.
     *
     * @param url The Firebase Storage URL.
     * @return The storage path or null if parsing fails.
     */
    private String getImagePathFromUrl(String url) {
        try {
            // Example URL: https://firebasestorage.googleapis.com/v0/b/yourapp.appspot.com/o/cars%2FcarId%2Ffilename.jpg?alt=media&token=...
            String path = url.split("o/")[1].split("\\?")[0];
            return path.replace("%2F", "/");
        } catch (Exception e) {
            Log.e("EditCarFragment", "Failed to parse image URL: " + url, e);
            return null;
        }
    }

    /**
     * Navigate back to AdminDashboardActivity after updating the car.
     */
    private void navigateToAdminDashboard() {
        Intent intent = new Intent(getContext(), AdminDashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
