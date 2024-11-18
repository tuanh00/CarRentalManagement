package com.example.carrentalapp.uiactivities.admin;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carrentalapp.R;
import com.example.carrentalapp.common.ImagePreviewAdapter;
import com.example.carrentalapp.states.car.CarAvailabilityState;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.example.carrentalapp.BuildConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AddCarFragment extends Fragment {

    private static final int AUTOCOMPLETE_REQUEST_CODE = 100;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    private EditText inputCarModel, inputCarBrand, inputCarSeats, inputCarLocation, inputCarPrice;
    private ArrayList<Uri> imageUris;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Button buttonSelectImages, buttonSubmitCarDetails;
    private RecyclerView imageRecyclerView;
    private ImagePreviewAdapter imagePreviewAdapter;
    private PlacesClient placesClient;
    private double latitude = 0.0;
    private double longitude = 0.0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the Google Maps API key is present
        if (TextUtils.isEmpty(BuildConfig.GOOGLE_MAPS_API_KEY)) {
            Log.e("AddCarFragment", "Google Maps API key is missing");
            Toast.makeText(getContext(), "Error: Google Maps API key is missing. Please check your configuration.", Toast.LENGTH_LONG).show();
            return; // Exit the method to prevent further execution
        }

        // Initialize Places API
        if (!Places.isInitialized()) {
            Log.d("AddCarFragment", "Initializing Places API");
            //Places.initialize(getContext(), getString(R.string.google_maps_key));
            Places.initialize(getContext(), BuildConfig.GOOGLE_MAPS_API_KEY);
        }
        placesClient = Places.createClient(getContext());

        // Initialize the Places API with your API key
        if (!Places.isInitialized()) {
            Places.initialize(getContext(), BuildConfig.GOOGLE_MAPS_API_KEY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        checkLocationPermission();
        View view = inflater.inflate(R.layout.fragment_add_car, container, false);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        inputCarModel = view.findViewById(R.id.inputCarModel);
        inputCarBrand = view.findViewById(R.id.inputCarBrand);
        inputCarSeats = view.findViewById(R.id.inputCarSeats);
        inputCarLocation = view.findViewById(R.id.inputCarLocation);
        inputCarPrice = view.findViewById(R.id.inputCarPrice);
        buttonSelectImages = view.findViewById(R.id.buttonSelectImages);
        buttonSubmitCarDetails = view.findViewById(R.id.buttonSubmitCarDetails);
        imageRecyclerView = view.findViewById(R.id.imageRecyclerView);

        imageUris = new ArrayList<>();
        imagePreviewAdapter = new ImagePreviewAdapter(getContext(), imageUris, position -> confirmImageRemoval(position));
        imageRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        imageRecyclerView.setAdapter(imagePreviewAdapter);

        buttonSelectImages.setOnClickListener(v -> selectImages());
        buttonSubmitCarDetails.setOnClickListener(v -> uploadCarDetails());

        // Set focus listener to launch Autocomplete when inputCarLocation is focused
        inputCarLocation.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                openPlaceAutocomplete();
            }
        });

        return view;
    }

    /**
     * Allows admin to select multiple images from the device.
     */
    private void selectImages() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), 1);
    }

    /**
     * Handles the result from image selection and place autocomplete.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            inputCarLocation.setText(place.getName()); // Set the location name in EditText
            if (place.getLatLng() != null) {
                latitude = place.getLatLng().latitude;
                longitude = place.getLatLng().longitude;
            }
        } else if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) { // multiple images selected
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    imageUris.add(data.getClipData().getItemAt(i).getUri());
                }
            } else if (data.getData() != null) { // single image selected
                imageUris.add(data.getData());
            }
            imagePreviewAdapter.notifyDataSetChanged();
        }
    }
    /**
     * Uploads car details to Firebase Firestore and images to Firebase Storage.
     */
    private void uploadCarDetails() {
        String model = inputCarModel.getText().toString().trim();
        String brand = inputCarBrand.getText().toString().trim();
        int seats = Integer.parseInt(inputCarSeats.getText().toString().trim());
        double price = Double.parseDouble(inputCarPrice.getText().toString().trim());
        String location = inputCarLocation.getText().toString().trim();

        if (TextUtils.isEmpty(model) || TextUtils.isEmpty(brand) || TextUtils.isEmpty(Integer.toString(seats)) || TextUtils.isEmpty(Double.toString(price)) || TextUtils.isEmpty(location)) {
            Toast.makeText(getContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!imageUris.isEmpty()) {
            uploadImages(model, brand, seats, price, location);
        } else {
            ArrayList<String> imageUrls = new ArrayList<>();
            saveCarToDatabase(model, brand, seats, price, location, imageUrls);
        }
    }

    /**
     * Uploads selected images to Firebase Storage and retrieves their URLs.
     */
    private void uploadImages(String model, String brand, int seats, double price, String location) {
        ArrayList<String> imageUrls = new ArrayList<>();
        final int totalImages = imageUris.size();
        final int[] uploadedImages = {0};

        for (Uri uri : imageUris) {
            String fileName = UUID.randomUUID().toString();
            StorageReference storageRef = storage.getReference().child("cars/" + fileName);
            storageRef.putFile(uri)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                            .addOnSuccessListener(downloadUri -> {
                                imageUrls.add(downloadUri.toString());
                                uploadedImages[0]++;
                                if (uploadedImages[0] == totalImages) {
                                    saveCarToDatabase(model, brand, seats, price, location, imageUrls);
                                }
                            }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to upload images: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    /**
     * Saves car details to Firestore with generated ID and state.
     */
    private void saveCarToDatabase(String model, String brand, int seats, double price, String location, ArrayList<String> images) {
        Map<String, Object> carData = new HashMap<>();
        carData.put("model", model);
        carData.put("brand", brand);
        carData.put("seats", seats);
        carData.put("price", price);
        carData.put("location", location);
        carData.put("images", images); // Store full URLs here
        carData.put("rating", 0f);
        carData.put("ratingCount", 0);
        carData.put("createdAt", FieldValue.serverTimestamp());
        carData.put("state", CarAvailabilityState.AVAILABLE.toString());

        db.collection("Cars").add(carData)
                .addOnSuccessListener(documentReference -> {
                    String carId = documentReference.getId(); // Get the document ID
                    documentReference.update("carId", carId);

                    Toast.makeText(getContext(), "Car added successfully", Toast.LENGTH_SHORT).show();
                    clearFields();

                    // Navigate to ViewCarsFragment
                    navigateToViewCarsFragment();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to add car: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Clears input fields and image list after successful upload.
     */
    private void clearFields() {
        inputCarModel.setText("");
        inputCarBrand.setText("");
        inputCarSeats.setText("");
        inputCarLocation.setText("");
        inputCarPrice.setText("");
        imageUris.clear();
        imagePreviewAdapter.notifyDataSetChanged();
    }

    /**
     * Opens the Google Places Autocomplete widget for location selection.
     */
    private void openPlaceAutocomplete() {
        // Define the fields to retrieve from the Autocomplete API
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

        // Create an Intent to launch the Autocomplete widget
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(getContext());

        // Start the activity for result, which will be handled in onActivityResult
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
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
                    imageUris.remove(position);
                    imagePreviewAdapter.notifyDataSetChanged();
                })
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Checks and requests location permission if not granted.
     */
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Handles the result of permission requests.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Location permission is needed for autocomplete", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    /**
     * Navigate back to ViewCarsFragment after adding a car.
     */
    private void navigateToViewCarsFragment() {
        ViewCarsFragment viewCarsFragment = new ViewCarsFragment();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.adminFragmentContainer, viewCarsFragment)
                .addToBackStack(null)
                .commit();
    }
}
