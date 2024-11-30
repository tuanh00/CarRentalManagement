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
import com.example.carrentalapp.states.car.CarAvailabilityState;
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

    private TextView contractIdTextView, userFullNameTextView, userEmailTextView, carIdTextView,
            startDateTextView, endDateTextView, totalPaymentTextView, statusTextView, createdAtTextView,
            updatedAtTextView, toggleStatusLabel;
    private SwitchCompat statusSwitch;
    private Button buttonUpdateStatus;
    private FirebaseFirestore db;
    private String contractId;
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
        contractIdTextView = view.findViewById(R.id.textViewContractId);
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
            contractId = bundle.getString("contractId", "N/A");
            String userFullName = bundle.getString("fullName", "N/A");
            String userEmail = bundle.getString("email", "N/A");
            String carId = bundle.getString("carId", "N/A");
            Timestamp startDate = bundle.getParcelable("startDate");
            Timestamp endDate = bundle.getParcelable("endDate");
            Timestamp createdAt = bundle.getParcelable("createdAt");
            Timestamp updateDate = bundle.getParcelable("updateDate");
            totalPayment = bundle.getDouble("totalPayment", 0.0);
            String statusString = bundle.getString("status", "N/A");
            try {
                currentStatus = ContractState.valueOf(statusString.toUpperCase());
            } catch (IllegalArgumentException e) {
                currentStatus = ContractState.ACTIVE; // Default to ACTIVE if status is invalid
            }

            // Set UI with values
            contractIdTextView.setText(contractId);
            userFullNameTextView.setText(userFullName);
            userEmailTextView.setText(userEmail);
            carIdTextView.setText(carId);
            startDateTextView.setText(formatTimestamp(startDate));
            endDateTextView.setText(formatTimestamp(endDate)); // Display end date
            createdAtTextView.setText(formatTimestamp(createdAt));
            updatedAtTextView.setText(formatTimestamp(updateDate));
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
        if (TextUtils.isEmpty(contractId) || "N/A".equals(contractId)) {
            Toast.makeText(getContext(), "Invalid Contract ID", Toast.LENGTH_SHORT).show();
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
        updates.put("updateDate", FieldValue.serverTimestamp());

        // Update the document using the contractId
        db.collection("Contracts").document(contractId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Contract status updated to " + newStatus, Toast.LENGTH_SHORT).show();

                    // If the new status is COMPLETED or CANCELED, update the car status to AVAILABLE
                    if (newStatus == ContractState.COMPLETED || newStatus == ContractState.CANCELED) {
                        String carId = carIdTextView.getText().toString(); // Retrieve the car ID from the UI
                        if (!TextUtils.isEmpty(carId) && !"N/A".equals(carId)) {
                            updateCarStatusToAvailable(carId);
                        } else {
                            Toast.makeText(getContext(), "Car ID is invalid or not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    // Notify the ViewContractsFragment of the update
                    getParentFragmentManager().setFragmentResult("contractUpdated", new Bundle());

                    // Navigate back to the ViewContractsFragment
                    navigateToViewContractFragment();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to update contract: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Updates the car status to AVAILABLE in Firestore.
     */
    private void updateCarStatusToAvailable(String carId) {
        Map<String, Object> carUpdates = new HashMap<>();
        carUpdates.put("state", CarAvailabilityState.AVAILABLE.toString());

        db.collection("Cars").document(carId).update(carUpdates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Car status updated to AVAILABLE", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to update car status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
