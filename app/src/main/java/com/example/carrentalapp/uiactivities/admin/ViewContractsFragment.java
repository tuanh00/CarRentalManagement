package com.example.carrentalapp.uiactivities.admin;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.carrentalapp.R;
import com.example.carrentalapp.common.ContractAdapter;
import com.example.carrentalapp.models.Contract;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class ViewContractsFragment extends Fragment {

    private RecyclerView recyclerViewContracts;
    private ContractAdapter contractAdapter;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ArrayList<Contract> contractList;
    private static final String PREFS_NAME = "CarRentalAppPrefs";
    private static final String ROLE_KEY = "user_role";
    private ListenerRegistration contractListener; // Listen to real-time adding latest contract
    private TextView noContractsTextView;

    public ViewContractsFragment() {
        // Required empty public constructor
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_contract, container, false);

        // Initialize Firebase and adapter
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        contractList = new ArrayList<>();

        recyclerViewContracts = view.findViewById(R.id.recyclerViewContracts);
        recyclerViewContracts.setLayoutManager(new LinearLayoutManager(getContext()));
        contractAdapter = new ContractAdapter(getContext(), contractList);
        recyclerViewContracts.setAdapter(contractAdapter);

        // Initialize the TextView for no contracts
        noContractsTextView = view.findViewById(R.id.noContractsTextView);

        // Initial load when the view is created
        loadContractsBasedOnRole();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove the listener when the view is destroyed to prevent memory leaks
        if (contractListener != null) {
            contractListener.remove();
            contractListener = null;
        }
    }
    private void loadContractsBasedOnRole() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String role = sharedPreferences.getString(ROLE_KEY, "customer");
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if("admin".equals(role)) {
            loadAllContracts();
        } else if("customer".equals(role) && userId != null) {
            loadUserContracts(userId);
        } else {
            Log.e("ViewContractsFragment", "Error: Role not recognized or user ID is null.");
        }
    }
    private void loadAllContracts() {
        // Remove existing listener if any
        if (contractListener != null) {
            contractListener.remove();
        }

        contractListener = db.collection("Contracts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Toast.makeText(getContext(), "Error loading contracts.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (querySnapshot != null) {
                        contractList.clear();
                        for (DocumentSnapshot document: querySnapshot.getDocuments()) {
                            Contract contract = document.toObject(Contract.class);
                            contract.setId(document.getId());
                            contractList.add(contract);
                        }
                        updateUI();
                    }
                });
    }
    private void loadUserContracts(String userId) {
        // Remove existing listener if any
        if (contractListener != null) {
            contractListener.remove();
        }

        contractListener = db.collection("Contracts")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.e("ViewContractsFragment", "Error loading user contracts: " + e.getMessage());
                        Toast.makeText(getContext(), "Error loading user contracts.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (querySnapshot != null) {
                        contractList.clear();
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            Contract contract = document.toObject(Contract.class);
                            contract.setId(document.getId());
                            contractList.add(contract);
                        }
                        updateUI();
                    }
                });
    }
    /**
     * Updates the UI based on whether contracts are available or not.
     */
    private void updateUI() {
        if (contractList.isEmpty()) {
            recyclerViewContracts.setVisibility(View.GONE);
            noContractsTextView.setVisibility(View.VISIBLE);
        } else {
            recyclerViewContracts.setVisibility(View.VISIBLE);
            noContractsTextView.setVisibility(View.GONE);
            contractAdapter.notifyDataSetChanged();
        }
    }
}