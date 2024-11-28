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
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ViewContractDetailsFragment extends Fragment {

    private TextView contractIdTextView, eventIdTextView, userFullNameTextView, userEmailTextView, carIdTextView, startDateTextView, endDateTextView, totalPaymentTextView, statusTextView, createdAtTextView;
    private String contractId;
    private double totalPayment;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_contract_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        contractIdTextView = view.findViewById(R.id.textViewContractId);
        eventIdTextView = view.findViewById(R.id.textViewEventId);
        userFullNameTextView = view.findViewById(R.id.textViewUserFullName);
        userEmailTextView = view.findViewById(R.id.textViewUserEmail);
        carIdTextView = view.findViewById(R.id.textViewCarId);
        startDateTextView = view.findViewById(R.id.textViewStartDate);
        endDateTextView = view.findViewById(R.id.textViewEndDate);
        createdAtTextView = view.findViewById(R.id.textViewCreatedAt);
        totalPaymentTextView = view.findViewById(R.id.textViewTotalPayment);
        statusTextView = view.findViewById(R.id.textViewStatus);

        // Retrieve contract data from arguments
        Bundle bundle = getArguments();
        if (bundle != null) {
            contractId = bundle.getString("contractId", "N/A");
            String eventId =  bundle.getString("eventId", "N/A");
            String userFullName = bundle.getString("fullName", "N/A");
            String userEmail = bundle.getString("email", "N/A");
            String carId = bundle.getString("carId", "N/A");
            Timestamp startDate = bundle.getParcelable("startDate");
            Timestamp endDate = bundle.getParcelable("endDate");
            Timestamp createdAt = bundle.getParcelable("createdAt");
            totalPayment = bundle.getDouble("totalPayment", 0.0);
            String status = bundle.getString("status", "N/A");

            // Set UI with values
            contractIdTextView.setText(contractId);
            eventIdTextView.setText(eventId);
            userFullNameTextView.setText(userFullName);
            userEmailTextView.setText(userEmail);
            carIdTextView.setText(carId);
            startDateTextView.setText(formatTimestamp(startDate));
            endDateTextView.setText(formatTimestamp(endDate));
            createdAtTextView.setText(formatTimestamp(createdAt));
            totalPaymentTextView.setText("Total Payment: $" + totalPayment);
            statusTextView.setText(status);

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
}
