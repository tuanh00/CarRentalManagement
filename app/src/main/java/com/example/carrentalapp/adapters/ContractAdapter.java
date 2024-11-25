package com.example.carrentalapp.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
// Don't forget to import Toast
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carrentalapp.R;
import com.example.carrentalapp.models.Contract;
import com.example.carrentalapp.models.User;
import com.example.carrentalapp.uiactivities.admin.EditContractFragment;
import com.example.carrentalapp.uiactivities.customer.ViewContractDetailsFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ContractAdapter extends RecyclerView.Adapter<ContractAdapter.ContractViewHolder> {

    private List<Contract> contractList;
    private Context context;
    private FirebaseFirestore db;
    private static final String PREFS_NAME = "CarRentalAppPrefs";
    private static final String ROLE_KEY = "user_role";
    private String role;
    private String loggedInFirstName;
    private String loggedInLastName;
    private String loggedInEmail;
    private Map<String, User> userCache;

    public ContractAdapter(Context context, List<Contract> contractList) {
        this.context = context;
        this.contractList = contractList;
        this.db = FirebaseFirestore.getInstance();
        this.userCache = new HashMap<>();

        // Retrieve role, first name, last name, email from SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.role = sharedPreferences.getString(ROLE_KEY, "customer");
        this.loggedInFirstName = sharedPreferences.getString("first_name", "N/A");
        this.loggedInLastName = sharedPreferences.getString("last_name", "N/A");
        this.loggedInEmail = sharedPreferences.getString("email", "N/A");
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

        if ("admin".equals(role)) {
            // Fetch user data from Firestore based on userId in contract
            String userId = contract.getUserId();

            holder.viewDetailsButton.setVisibility(View.GONE);
            holder.editContractButton.setVisibility(View.VISIBLE);

            // Initialize with loading placeholders
            holder.userAccountTextView.setText("Email: Loading...");
            holder.userNameTextView.setText("User: Loading...");

            getUserData(userId, user -> {
                if (user != null) {
                    holder.userAccountTextView.setText("Email: " + user.getEmail());
                    holder.userNameTextView.setText("User: " + user.getFirstName() + " " + user.getLastName());

                    // Set up click listener after user data is available
                    holder.editContractButton.setOnClickListener(v -> {
                        FragmentActivity fragmentActivity = (FragmentActivity) context;
                        EditContractFragment editFragment = new EditContractFragment();

                        // Pass the contract data through the bundle, including fullName and email
                        Bundle bundle = createContractBundle(contract);
                        String fullName = (user.getFirstName() != null ? user.getFirstName() : "") + " " +
                                (user.getLastName() != null ? user.getLastName() : "");
                        bundle.putString("fullName", fullName.trim());
                        bundle.putString("email", user.getEmail());

                        editFragment.setArguments(bundle);

                        fragmentActivity.getSupportFragmentManager().beginTransaction()
                                .replace(R.id.adminFragmentContainer, editFragment)
                                .addToBackStack(null)
                                .commit();
                    });
                } else {
                    holder.userAccountTextView.setText("Email: N/A");
                    holder.userNameTextView.setText("User: N/A");

                    holder.editContractButton.setOnClickListener(v -> {
                        Toast.makeText(context, "User data not available", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            // For customer
            holder.userNameTextView.setText("User: " + loggedInFirstName + " " + loggedInLastName);
            holder.userAccountTextView.setText("Email: " + loggedInEmail);

            holder.viewDetailsButton.setVisibility(View.VISIBLE);
            holder.editContractButton.setVisibility(View.GONE);

            // Set up click listener
            holder.viewDetailsButton.setOnClickListener(v -> {
                FragmentActivity fragmentActivity = (FragmentActivity) context;
                ViewContractDetailsFragment detailsFragment = new ViewContractDetailsFragment();

                // Pass the contract data through the bundle, including fullName and email
                Bundle bundle = createContractBundle(contract);
                String fullName = (loggedInFirstName != null ? loggedInFirstName : "") + " " +
                        (loggedInLastName != null ? loggedInLastName : "");
                bundle.putString("fullName", fullName.trim());
                bundle.putString("email", loggedInEmail);

                detailsFragment.setArguments(bundle);

                fragmentActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.customerFragmentContainer, detailsFragment)
                        .addToBackStack(null)
                        .commit();
            });
        }

        // Set other contract details
        holder.carIdTextView.setText("Car ID: " + contract.getCarId());
        holder.startDateTextView.setText("Start: " + formatTimestamp(contract.getStartDate()));
        holder.endDateTextView.setText("End: " + formatTimestamp(contract.getEndDate()));
        holder.createdAtTextView.setText("Created At: " + formatTimestamp(contract.getCreatedAt()));
        holder.updatedAtTextView.setText("Updated At: " + formatTimestamp(contract.getUpdatedAt()));
        holder.totalPaymentTextView.setText("Total: $" + contract.getTotalPayment());
        holder.statusTextView.setText("Status: " + contract.getState());
    }

    @Override
    public int getItemCount() {
        return contractList.size();
    }

    private void getUserData(String userId, UserCallback callback) {
        if (userCache.containsKey(userId)) {
            callback.onUserFetched(userCache.get(userId));
        } else {
            db.collection("Users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String email = documentSnapshot.getString("email");
                            String firstName = documentSnapshot.getString("firstName");
                            String lastName = documentSnapshot.getString("lastName");

                            User user = new User();
                            user.setEmail(email);
                            user.setFirstName(firstName);
                            user.setLastName(lastName);

                            userCache.put(userId, user);

                            callback.onUserFetched(user);
                        } else {
                            callback.onUserFetched(null);
                        }
                    })
                    .addOnFailureListener(e -> {
                        callback.onUserFetched(null);
                    });
        }
    }

    private interface UserCallback {
        void onUserFetched(User user);
    }

    private Bundle createContractBundle(Contract contract) {
        Bundle bundle = new Bundle();
        bundle.putString("eventId", contract.getEventId());
        bundle.putString("userId", contract.getUserId());
        bundle.putString("carId", contract.getCarId());
        bundle.putParcelable("startDate", contract.getStartDate()); // Pass Timestamp directly
        bundle.putParcelable("endDate", contract.getEndDate());
        bundle.putParcelable("createdAt", contract.getCreatedAt());
        bundle.putParcelable("updatedAt", contract.getUpdatedAt());
        bundle.putDouble("totalPayment", contract.getTotalPayment());
        bundle.putString("status", contract.getState().toString());
        return bundle;
    }

    /**
     * Formats Firebase Timestamp to a readable date string.
     */
    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) return "N/A";
        Date date = timestamp.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    public static class ContractViewHolder extends RecyclerView.ViewHolder {
        TextView userAccountTextView, userNameTextView, carIdTextView, startDateTextView, endDateTextView, totalPaymentTextView, statusTextView, createdAtTextView, updatedAtTextView;
        Button viewDetailsButton, editContractButton;

        public ContractViewHolder(@NonNull View itemView) {
            super(itemView);
            userAccountTextView = itemView.findViewById(R.id.textViewUserAccount);
            userNameTextView = itemView.findViewById(R.id.textViewUserName);
            carIdTextView = itemView.findViewById(R.id.textViewCarId);
            startDateTextView = itemView.findViewById(R.id.textViewStartDate);
            endDateTextView = itemView.findViewById(R.id.textViewEndDate);
            createdAtTextView = itemView.findViewById(R.id.textViewCreatedAt);
            updatedAtTextView = itemView.findViewById(R.id.textViewUpdatedAt);
            totalPaymentTextView = itemView.findViewById(R.id.textViewTotalPayment);
            statusTextView = itemView.findViewById(R.id.textViewStatus);
            viewDetailsButton = itemView.findViewById(R.id.buttonViewDetails);
            editContractButton = itemView.findViewById(R.id.buttonEditContract);
        }
    }

}
