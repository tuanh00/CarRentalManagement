package com.example.carrentalapp.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import com.example.carrentalapp.states.contract.ContractState;
import com.example.carrentalapp.uiactivities.admin.EditContractFragment;
import com.example.carrentalapp.uiactivities.customer.RatingDialog;
import com.example.carrentalapp.uiactivities.customer.ViewContractDetailsFragment;
import com.google.firebase.firestore.FieldValue;
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
    private String role, loggedInFirstName, loggedInLastName, loggedInEmail;
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
        String contractId = contract.getId();

        // Set up contract details
        holder.carIdTextView.setText("Car ID: " + contract.getCarId());
        holder.startDateTextView.setText("Start: " + formatTimestamp(contract.getStartDate()));
        holder.endDateTextView.setText("End: " + formatTimestamp(contract.getEndDate()));
        holder.createdAtTextView.setText("Created At: " + formatTimestamp(contract.getCreatedAt()));
        holder.updatedAtTextView.setText("Updated At: " + formatTimestamp(contract.getUpdatedAt()));
        holder.totalPaymentTextView.setText("Total: $" + contract.getTotalPayment());
        holder.statusTextView.setText("Status: " + contract.getState());

        if ("admin".equals(role)) {
            //Admin side
            holder.viewDetailsButton.setVisibility(View.GONE);
            holder.editContractButton.setVisibility(View.VISIBLE);
            holder.rateEperienceButton.setVisibility(View.GONE);

            // Loading placeholders while fetching data
            holder.userAccountTextView.setText("Email: Loading...");
            holder.userNameTextView.setText("User: Loading...");

            // Fetch user data and set up the Edit button
            getUserData(contract.getUserId(), user -> {
                if (user != null) {
                    holder.userAccountTextView.setText("Email: " + user.getEmail());
                    holder.userNameTextView.setText("User: " + user.getFirstName() + " " + user.getLastName());
                    // Click listener after user data is available
                    holder.editContractButton.setOnClickListener(v -> {
                        FragmentActivity fragmentActivity = (FragmentActivity) context;
                        EditContractFragment editFragment = new EditContractFragment();
                        // Pass the contract data through the bundle
                        Bundle bundle = createContractBundle(contract, contractId);
                        bundle.putString("fullName", user.getFirstName() + " " + user.getLastName());
                        bundle.putString("email", user.getEmail());
                        editFragment.setArguments(bundle);
                        fragmentActivity.getSupportFragmentManager().beginTransaction()
                                .replace(R.id.adminFragmentContainer, editFragment)
                                .addToBackStack(null)
                                .commit();
                    });
                }
            });
        } else {
            // Customer side
            holder.viewDetailsButton.setVisibility(View.VISIBLE);
            holder.editContractButton.setVisibility(View.GONE);

            holder.userNameTextView.setText("User: " + loggedInFirstName + " " + loggedInLastName);
            holder.userAccountTextView.setText("Email: " + loggedInEmail);

            // Always show the "Rate Experience" button if contract is COMPLETED
            if (contract.getState() == ContractState.COMPLETED) {
                holder.rateEperienceButton.setVisibility(View.VISIBLE);
                holder.rateEperienceButton.setOnClickListener(v -> openRatingDialog(contractId, contract.getCarId(), contract.isRated()));
                Log.d("ContractAdapter", "Showing Rate Experience button for Contract ID: " + contractId);
            } else {
                holder.rateEperienceButton.setVisibility(View.GONE);
            }

            // Click listener after user data is available
            holder.viewDetailsButton.setOnClickListener(v -> {
                FragmentActivity fragmentActivity = (FragmentActivity) context;
                ViewContractDetailsFragment detailsFragment = new ViewContractDetailsFragment();

                // Pass the contract data through the bundle
                Bundle bundle = createContractBundle(contract, contractId);
                bundle.putString("fullName", loggedInFirstName + " " + loggedInLastName);
                bundle.putString("email", loggedInEmail);
                detailsFragment.setArguments(bundle);
                fragmentActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.customerFragmentContainer, detailsFragment)
                        .addToBackStack(null)
                        .commit();
            });
        }


    }

    private void openRatingDialog(String contractId, String carId, boolean isRated) {
        if (isRated) {
            Toast.makeText(context, "You have already rated this contract!", Toast.LENGTH_SHORT).show();
            return;
        }

        RatingDialog ratingDialog = new RatingDialog(context, rating -> {
            db.collection("Cars").document(carId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Fetch current rating data or initialize them
                            double currentRating = documentSnapshot.contains("rating")
                                    ? documentSnapshot.getDouble("rating")
                                    : 0.0;
                            long ratingCount = documentSnapshot.contains("ratingCount")
                                    ? documentSnapshot.getLong("ratingCount")
                                    : 0;

                            // Calculate new rating
                            double newRating = ((currentRating * ratingCount) + rating) / (ratingCount + 1);
                            long newRatingCount = ratingCount + 1;

                            // Update the database
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("rating", newRating);
                            updates.put("ratingCount", newRatingCount);
                            updates.put("ratedBy", FieldValue.arrayUnion(loggedInEmail)); // Save user's email

                            db.collection("Cars").document(carId).update(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        // Mark the contract as rated
                                        db.collection("Contracts").document(contractId).update("rated", true)
                                                .addOnSuccessListener(r -> Toast.makeText(context, "Rating submitted successfully!", Toast.LENGTH_SHORT).show())
                                                .addOnFailureListener(e -> Toast.makeText(context, "Failed to update contract rating status: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to submit rating: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to fetch car data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        ratingDialog.show();
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

    private Bundle createContractBundle(Contract contract, String contractId) {
        Bundle bundle = new Bundle();
        bundle.putString("contractId", contractId);
        bundle.putString("eventId", contract.getEventId() != null ? contract.getEventId() : "N/A");
        bundle.putString("userId", contract.getUserId());
        bundle.putString("carId", contract.getCarId());
        bundle.putParcelable("startDate", contract.getStartDate());
        bundle.putParcelable("endDate", contract.getEndDate());
        bundle.putParcelable("createdAt", contract.getCreatedAt());
        bundle.putParcelable("updateDate", contract.getUpdatedAt());
        bundle.putDouble("totalPayment", contract.getTotalPayment());
        bundle.putString("status", contract.getState().toString());
        return bundle;
    }


    public static class ContractViewHolder extends RecyclerView.ViewHolder {
        TextView userAccountTextView, userNameTextView, carIdTextView, startDateTextView, endDateTextView, totalPaymentTextView, statusTextView, createdAtTextView, updatedAtTextView;
        Button viewDetailsButton, editContractButton, rateEperienceButton;

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
            rateEperienceButton = itemView.findViewById(R.id.buttonRateExperience);
        }
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




}
