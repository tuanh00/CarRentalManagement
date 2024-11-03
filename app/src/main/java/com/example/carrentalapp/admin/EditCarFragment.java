package com.example.carrentalapp.admin;

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

import com.example.carrentalapp.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditCarFragment extends Fragment {

    private EditText brandEditText, modelEditText, seatsEditText, priceEditText;
    private Button saveButton;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_car, container, false);

        brandEditText = view.findViewById(R.id.brandEditText);
        modelEditText = view.findViewById(R.id.modelEditText);
        seatsEditText = view.findViewById(R.id.seatsEditText);
        priceEditText = view.findViewById(R.id.priceEditText);
        saveButton = view.findViewById(R.id.saveButton);

        db = FirebaseFirestore.getInstance();

        saveButton.setOnClickListener(v -> saveCar());

        return view;
    }

    private void saveCar() {
        String brand = brandEditText.getText().toString();
        String model = modelEditText.getText().toString();
        String seatsStr = seatsEditText.getText().toString();
        String priceStr = priceEditText.getText().toString();

        if (TextUtils.isEmpty(brand) || TextUtils.isEmpty(model) || TextUtils.isEmpty(seatsStr) || TextUtils.isEmpty(priceStr)) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int seats = Integer.parseInt(seatsStr);
        double price = Double.parseDouble(priceStr);

        // Assuming you have the carId for editing
        String carId = getArguments().getString("carId");

        db.collection("Cars").document(carId)
                .update("brand", brand, "model", model, "seats", seats, "price", price)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Car updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update car: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
