package com.example.carrentalapp.common;

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
import android.widget.Toast;

import com.example.carrentalapp.R;
import com.example.carrentalapp.common.ContractAdapter;
import com.example.carrentalapp.models.Contract;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class ViewContractsFragment extends Fragment {

    private RecyclerView recyclerViewContracts;
    private ContractAdapter contractAdapter;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ArrayList<Contract> contractList;
    private static final String PREFS_NAME = "CarRentalAppPrefs";
    private static final String ROLE_KEY = "user_role";

    public ViewContractsFragment() {
        // Required empty public constructor
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_contract, container, false);
        recyclerViewContracts = view.findViewById(R.id.recyclerViewContracts);
        recyclerViewContracts.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize Firebase and adapter
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        contractList = new ArrayList<>();
        contractAdapter = new ContractAdapter(getContext(), contractList);
        recyclerViewContracts.setAdapter(contractAdapter);

        // Initial load when the view is created
        loadContractsBasedOnRole();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Listen for refresh signal from EditContractFragment
        getParentFragmentManager().setFragmentResultListener("contractUpdated", this, (requestKey, result) -> {
            // Reload contracts when an update occurs
            loadContractsBasedOnRole();
        });

        // Existing setup for RecyclerView and data loading
        recyclerViewContracts = view.findViewById(R.id.recyclerViewContracts);
        recyclerViewContracts.setLayoutManager(new LinearLayoutManager(getContext()));

        // Reload contracts when receiving the "contractUpdated" signal
        loadContractsBasedOnRole();
    }
    @Override
    public void onResume() {
        super.onResume();
        // Reload contracts when fragment resumes to ensure updated data
        loadContractsBasedOnRole();
    }


    private void loadContractsBasedOnRole() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String role = sharedPreferences.getString(ROLE_KEY, "customer");
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if ("admin".equals(role)) {
            loadAllContracts();
        } else if ("customer".equals(role) && userId != null) {
            loadUserContracts(userId);
        }  else {
            Log.e("ViewContractsFragment", "Error: Role not recognized or user ID is null.");
        }
    }

    private void loadAllContracts() {
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
                        contractAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Error loading contracts.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadUserContracts(String userId) {
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
                        contractAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("ViewContractsFragment", "Error loading user contracts: " + task.getException());
                        Log.e("ViewContractsFragment", "User ID: " + userId);

                        Toast.makeText(getContext(), "Error loading user contracts.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
