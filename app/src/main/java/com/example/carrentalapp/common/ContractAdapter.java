package com.example.carrentalapp.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carrentalapp.R;
import com.example.carrentalapp.models.Contract;
import com.example.carrentalapp.uiactivities.admin.EditContractFragment;
import com.example.carrentalapp.uiactivities.customer.ViewContractDetailsFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ContractAdapter extends RecyclerView.Adapter<ContractAdapter.ContractViewHolder> {

    private List<Contract> contractList;
    private Context context;
    private FirebaseFirestore db;
    private static final String PREFS_NAME = "CarRentalAppPrefs";
    private static final String ROLE_KEY = "user_role";
    private String role, firstName, lastName;

    public ContractAdapter(Context context, List<Contract> contractList) {
        this.context = context;
        this.contractList = contractList;
        this.db = FirebaseFirestore.getInstance();

        // Retrieve role from SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.role = sharedPreferences.getString(ROLE_KEY, "customer");
        this.firstName = sharedPreferences.getString("first_name", "N/A");
        this.lastName = sharedPreferences.getString("last_name", "N/A");
    }

    @NonNull
    @Override
    public ContractViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contract, parent, false);
        return new ContractViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContractViewHolder holder, int position) {
        Contract contract = contractList.get(position);

        holder.userNameTextView.setText("User: " + firstName + " " + lastName);
        holder.carIdTextView.setText("Car ID: " + contract.getCarId());
        holder.startDateTextView.setText("Start: " + formatTimestamp(contract.getStartDate()));
        holder.endDateTextView.setText("End: " + formatTimestamp(contract.getEndDate()));
        holder.createdAtTextView.setText("Created At: " + formatTimestamp(contract.getCreatedAt()));
        holder.totalPaymentTextView.setText("Total: $" + contract.getTotalPayment());
        holder.statusTextView.setText("Status: " + contract.getState());

        if ("admin".equals(role)) {
            // Only show the Edit button for admins
            holder.viewDetailsButton.setVisibility(View.GONE);
            holder.editContractButton.setVisibility(View.VISIBLE);

            // Edit Contract Button Action (Only for Admins)
            holder.editContractButton.setOnClickListener(v -> {
                FragmentActivity fragmentActivity = (FragmentActivity) context;
                EditContractFragment editFragment = new EditContractFragment();

                // Pass the contract data through the bundle
                editFragment.setArguments(createContractBundle(contract));

                fragmentActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.adminFragmentContainer, editFragment)
                        .addToBackStack(null)
                        .commit();
            });
        } else {
            // Only show the View Details button for customers
            holder.viewDetailsButton.setVisibility(View.VISIBLE);
            holder.editContractButton.setVisibility(View.GONE);

            // Button to view details
            holder.viewDetailsButton.setOnClickListener(v -> {
                FragmentActivity fragmentActivity = (FragmentActivity) context;
                ViewContractDetailsFragment detailsFragment = new ViewContractDetailsFragment();
                detailsFragment.setArguments(createContractBundle(contract));
                fragmentActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.customerFragmentContainer, detailsFragment)
                        .addToBackStack(null)
                        .commit();
            });
        }
    }

    @Override
    public int getItemCount() {
        return contractList.size();
    }

    private Bundle createContractBundle(Contract contract) {
        Bundle bundle = new Bundle();
        // Assuming `eventId` is a field in `Contract`
        bundle.putString("eventId", contract.getEventId());
        bundle.putString("userId", contract.getUserId()); //pass but not use yet
        bundle.putString("fullName", firstName + lastName);
        bundle.putString("carId", contract.getCarId());
        bundle.putParcelable("startDate", contract.getStartDate()); // Pass Timestamp directly
        bundle.putParcelable("endDate", contract.getEndDate()); // Pass Timestamp directly
        bundle.putParcelable("createdAt", contract.getCreatedAt()); // Pass Timestamp directly
        bundle.putDouble("totalPayment", contract.getTotalPayment());
        bundle.putString("status", contract.getState().toString());
        return bundle;
    }

    /**
     * Formats Firebase Timestamp to a readable date string.
     *
     * @param timestamp The Firebase Timestamp.
     * @return Formatted date string.
     */
    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) return "N/A";
        Date date = timestamp.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    public static class ContractViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTextView, carIdTextView, startDateTextView, endDateTextView, totalPaymentTextView, statusTextView, createdAtTextView;
        Button viewDetailsButton, editContractButton;

        public ContractViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.textViewUserName);
            carIdTextView = itemView.findViewById(R.id.textViewCarId);
            startDateTextView = itemView.findViewById(R.id.textViewStartDate);
            endDateTextView = itemView.findViewById(R.id.textViewEndDate);
            createdAtTextView = itemView.findViewById(R.id.textViewCreatedAt);
            totalPaymentTextView = itemView.findViewById(R.id.textViewTotalPayment);
            statusTextView = itemView.findViewById(R.id.textViewStatus);
            viewDetailsButton = itemView.findViewById(R.id.buttonViewDetails);
            editContractButton = itemView.findViewById(R.id.buttonEditContract);
        }
    }

    public void loadContractsBasedOnRole(String role, String userId) {
        if ("admin".equals(role)) {
            // Fetch all contracts for admin
            db.collection("Contracts")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            contractList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Contract contract = document.toObject(Contract.class);
                                contractList.add(contract);
                            }
                            notifyDataSetChanged();
                        } else {
                            Log.e("ContractAdapter", "Error retrieving contracts", task.getException());
                        }
                    });
        } else if ("customer".equals(role) && userId != null) {
            // Fetch only the customer's contracts
            db.collection("Contracts")
                    .whereEqualTo("userId", userId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            contractList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Contract contract = document.toObject(Contract.class);
                                contractList.add(contract);
                            }
                            notifyDataSetChanged();
                        } else {
                            Log.e("ContractAdapter", "Error retrieving user contracts", task.getException());
                        }
                    });
        }
    }
}
