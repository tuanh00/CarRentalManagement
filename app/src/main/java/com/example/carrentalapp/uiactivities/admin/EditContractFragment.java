package com.example.carrentalapp.uiactivities.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.appcompat.widget.SwitchCompat;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.carrentalapp.R;
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

    private TextView eventIdTextView, userFullNameTextView, userEmailTextView, carIdTextView,
            startDateTextView, endDateTextView, totalPaymentTextView, statusTextView, createdAtTextView,
            updatedAtTextView, toggleStatusLabel;
    private SwitchCompat statusSwitch;
    private Button buttonUpdateStatus;
    private FirebaseFirestore db;
    private String eventId;
    private ContractState currentStatus, newStatus;
    private double totalPayment;

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
        userFullNameTextView = view.findViewById(R.id.textViewUserFullName);
        userEmailTextView = view.findViewById(R.id.textViewUserEmail);
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
            String userEmail = bundle.getString("email", "N/A");
            String carId = bundle.getString("carId", "N/A");
            Timestamp startDate = bundle.getParcelable("startDate");
            Timestamp endDate = bundle.getParcelable("endDate");
            Timestamp createdAt = bundle.getParcelable("createdAt");
            Timestamp updatedAt = bundle.getParcelable("updatedAt");
            totalPayment = bundle.getDouble("totalPayment", 0.0);
            String statusString = bundle.getString("status", "N/A");
            try {
                currentStatus = ContractState.valueOf(statusString.toUpperCase());
            } catch (IllegalArgumentException e) {
                currentStatus = ContractState.ACTIVE; // Default to ACTIVE if status is invalid
            }

            // Set UI with values
            eventIdTextView.setText(eventId);
            userFullNameTextView.setText(userFullName);
            userEmailTextView.setText(userEmail);
            carIdTextView.setText(carId);
            startDateTextView.setText(formatTimestamp(startDate));
            endDateTextView.setText(formatTimestamp(endDate)); // Display end date
            createdAtTextView.setText(formatTimestamp(createdAt));
            updatedAtTextView.setText(formatTimestamp(updatedAt));
            totalPaymentTextView.setText("$" + totalPayment);
            statusTextView.setText(currentStatus.toString());

            // Set status toggle label and switch text based on current status
            setupStatusToggle();

            buttonUpdateStatus.setOnClickListener(v -> updateContractStatus());
        } else {
            Toast.makeText(getContext(), "Failed to load contract details", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupStatusToggle() {
        switch (currentStatus) {
            case ACTIVE:
                toggleStatusLabel.setText("Set Status to Completed:");
                statusSwitch.setText("Completed");
                break;
            case COMPLETED:
                toggleStatusLabel.setText("Set Status to Canceled:");
                statusSwitch.setText("Canceled");
                break;
            case CANCELED:
                toggleStatusLabel.setText("Set Status to Active:");
                statusSwitch.setText("Active");
                break;
            default:
                toggleStatusLabel.setText("Set Status:");
                statusSwitch.setText("Unknown");
                break;
        }
        statusSwitch.setChecked(false); // Ensure the switch is unchecked initially
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

        if (!statusSwitch.isChecked()) {
            Toast.makeText(getContext(), "Please toggle the switch to confirm status update", Toast.LENGTH_SHORT).show();
            return;
        }

        // Determine the new status based on current status
        switch (currentStatus) {
            case ACTIVE:
                newStatus = ContractState.COMPLETED;
                break;
            case COMPLETED:
                newStatus = ContractState.CANCELED;
                break;
            case CANCELED:
                newStatus = ContractState.ACTIVE;
                break;
            default:
                Toast.makeText(getContext(), "Invalid current status", Toast.LENGTH_SHORT).show();
                return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus.toString());
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

                                    // Navigate back to the ViewContractsFragment
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
     * Navigate back to ViewContractsFragment after updating the contract.
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
