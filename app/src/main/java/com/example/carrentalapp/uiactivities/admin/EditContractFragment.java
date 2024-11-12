// EditContractFragment.java
package com.example.carrentalapp.uiactivities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
// Import SwitchCompat for status toggle
import androidx.appcompat.widget.SwitchCompat;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.carrentalapp.R;
import com.example.carrentalapp.common.ViewContractsFragment;
import com.example.carrentalapp.states.contract.ContractState;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditContractFragment extends Fragment {

    private TextView eventIdTextView, userFulleNameTextView, carIdTextView, startDateTextView, endDateTextView, totalPaymentTextView, statusTextView, createdAtTextView, updatedAtTextView, toggleStatusLabel;
    private SwitchCompat statusSwitch;
    private Button buttonUpdateStatus;
    private FirebaseFirestore db;
    private String eventId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_contract, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialize UI components
        eventIdTextView = view.findViewById(R.id.textViewContractId);
        userFulleNameTextView = view.findViewById(R.id.textViewUserFullName);
        carIdTextView = view.findViewById(R.id.textViewCarId);
        startDateTextView = view.findViewById(R.id.textViewStartDate);
        endDateTextView = view.findViewById(R.id.textViewEndDate);
        totalPaymentTextView = view.findViewById(R.id.textViewTotalPayment);
        statusTextView = view.findViewById(R.id.textViewCurrentStatus);
        createdAtTextView = view.findViewById(R.id.textViewCreatedAt);
        updatedAtTextView = view.findViewById(R.id.textViewUpdatedAt);
        toggleStatusLabel = view.findViewById(R.id.textViewToggleStatus);
        statusSwitch = view.findViewById(R.id.statusSwitch);
        buttonUpdateStatus = view.findViewById(R.id.buttonUpdateStatus);

        // Retrieve contract data from arguments
        Bundle bundle = getArguments();
        if (bundle != null) {
            eventId = bundle.getString("eventId", "N/A");
            String userFullName = bundle.getString("fullName", "N/A");
            String carId = bundle.getString("carId", "N/A");
            Timestamp startDate = bundle.getParcelable("startDate");
            Timestamp endDate = bundle.getParcelable("endDate");
            Timestamp createdAt = bundle.getParcelable("createdAt");
            Timestamp updatedAt = bundle.getParcelable("updatedAt");
            double totalPayment = bundle.getDouble("totalPayment", 0.0);
            String status = bundle.getString("status", "N/A");

            // Set UI with values
            eventIdTextView.setText(eventId);
            userFulleNameTextView.setText(userFullName);
            carIdTextView.setText(carId);
            startDateTextView.setText(formatTimestamp(startDate));
            endDateTextView.setText(formatTimestamp(endDate));
            createdAtTextView.setText(formatTimestamp(createdAt));
            updatedAtTextView.setText(formatTimestamp(updatedAt));
            totalPaymentTextView.setText("Total Payment: $" + totalPayment);
            statusTextView.setText(status);


            // Set status toggle label and switch text based on current status
            if (ContractState.ACTIVE.toString().equalsIgnoreCase(status)) {
                toggleStatusLabel.setText("Set Status to Completed:");
                statusSwitch.setText("Completed");
                statusSwitch.setChecked(false);
            } else if (ContractState.COMPLETED.toString().equalsIgnoreCase(status)) {
                toggleStatusLabel.setText("Set Status to Canceled:");
                statusSwitch.setText("Canceled");
                statusSwitch.setChecked(true);
            }

            buttonUpdateStatus.setOnClickListener(v -> updateContractStatus());
        } else {
            Toast.makeText(getContext(), "Failed to load contract details", Toast.LENGTH_SHORT).show();
        }
    }
    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) return "N/A";
        Date date = timestamp.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Updates the contract status in Firestore.
     */
    private void updateContractStatus() {
        if (TextUtils.isEmpty(eventId) || "N/A".equals(eventId)) {
            Toast.makeText(getContext(), "Invalid event ID", Toast.LENGTH_SHORT).show();
            return;
        }

        String newStatus = statusSwitch.isChecked() ? ContractState.COMPLETED.toString() : ContractState.CANCELED.toString();
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        updates.put("updatedAt", FieldValue.serverTimestamp());

        // Query Firestore to find the document ID by eventId
        db.collection("Contracts")
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get the first document that matches the eventId
                        String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Now update the document using the documentId
                        db.collection("Contracts").document(documentId).update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Contract status updated to " + newStatus, Toast.LENGTH_SHORT).show();

                                    // Set a result to notify ViewContractsFragment of the update
                                    getParentFragmentManager().setFragmentResult("contractUpdated", new Bundle());

                                    // Navigate back to the navigateToViewContractFragment
                                    navigateToViewContractFragment();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Failed to update contract: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(getContext(), "No contract found for event ID: " + eventId, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error finding contract by event ID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Navigate back to navigateToViewContractFragment after updating the contract.
     */
    private void navigateToViewContractFragment() {
        Fragment viewContractsFragment = new ViewContractsFragment();

        // Replace the current fragment with ViewContractsFragment
        getParentFragmentManager().beginTransaction()
                .replace(R.id.adminFragmentContainer, viewContractsFragment)
                .addToBackStack(null)
                .commit();
    }

}
