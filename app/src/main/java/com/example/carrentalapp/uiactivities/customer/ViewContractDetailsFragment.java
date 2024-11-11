// ViewContractDetailsFragment.java
package com.example.carrentalapp.uiactivities.customer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.carrentalapp.R;
import com.example.carrentalapp.models.Contract;

public class ViewContractDetailsFragment extends Fragment {

    private TextView userIdTextView, carIdTextView, startDateTextView, endDateTextView, totalPaymentTextView, statusTextView;
    private String contractId, userId, carId, startDate, endDate, status;
    private double totalPayment;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_contract_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userIdTextView = view.findViewById(R.id.textViewUserId);
        carIdTextView = view.findViewById(R.id.textViewCarId);
        startDateTextView = view.findViewById(R.id.textViewStartDate);
        endDateTextView = view.findViewById(R.id.textViewEndDate);
        totalPaymentTextView = view.findViewById(R.id.textViewTotalPayment);
        statusTextView = view.findViewById(R.id.textViewStatus);

        // Retrieve contract data from arguments
        Bundle bundle = getArguments();
        if (bundle != null) {
            contractId = bundle.getString("contractId", "N/A");
            userId = bundle.getString("userId", "N/A");
            carId = bundle.getString("carId", "N/A");
            startDate = bundle.getString("startDate", "N/A");
            endDate = bundle.getString("endDate", "N/A");
            totalPayment = bundle.getDouble("totalPayment", 0.0);
            status = bundle.getString("status", "N/A");

            // Populate UI elements with contract data
            userIdTextView.setText("User ID: " + userId);
            carIdTextView.setText("Car ID: " + carId);
            startDateTextView.setText("Start Date: " + startDate);
            endDateTextView.setText("End Date: " + endDate);
            totalPaymentTextView.setText("Total Payment: $" + totalPayment);
            statusTextView.setText("Status: " + status);
        } else {
            Toast.makeText(getContext(), "Failed to load contract details", Toast.LENGTH_SHORT).show();
        }
    }
}
