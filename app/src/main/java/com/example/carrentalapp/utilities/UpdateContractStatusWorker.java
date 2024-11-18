package com.example.carrentalapp.utilities;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.carrentalapp.states.car.CarAvailabilityState;
import com.example.carrentalapp.states.contract.ContractState;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class UpdateContractStatusWorker extends Worker {

    private FirebaseFirestore db;

    public UpdateContractStatusWorker(@NonNull Context context, @NonNull WorkerParameters workerParams, FirebaseFirestore db) {
        super(context, workerParams);
        this.db = db;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Timestamp now = Timestamp.now();
            db.collection("Contracts")
                    .whereLessThanOrEqualTo("endDate", now)
                    .whereEqualTo("status", ContractState.ACTIVE)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String contractId = document.getId();
                            String carId = document.getString("carId");

                            //Update contracts status to COMPLETED
                            Map<String, Object> contractUpdates = new HashMap<>();
                            contractUpdates.put("status", ContractState.COMPLETED);
                            contractUpdates.put("updatedAt", now);

                            db.collection("Contracts").document(contractId)
                                    .update(contractUpdates)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this.getApplicationContext(), contractId + " updated to COMPLETED.", Toast.LENGTH_SHORT).show();

                                        //update car availability
                                        Map<String, Object> carUpdates = new HashMap<>();
                                        carUpdates.put("state", CarAvailabilityState.AVAILABLE);
                                        db.collection("Cars").document(carId)
                                                .update(carUpdates)
                                                .addOnSuccessListener(aVoid1 -> {
                                                    Toast.makeText(this.getApplicationContext(), "Car ID: " + carId + " set to Available.", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(this.getApplicationContext(), "Failed to update car ID: " + carId + ". Error: " + e, Toast.LENGTH_SHORT).show();
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this.getApplicationContext(), "Failed to update contract ID: " + contractId, Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this.getApplicationContext(), "Failed to fetch contracts. Error: " + e, Toast.LENGTH_SHORT).show();
                    });
                            return Result.success();
        } catch (Exception e) {
            Log.e("UpdateContractStatus", "Error in doWork", e);
            Toast.makeText(this.getApplicationContext(), "Error in doWork", Toast.LENGTH_SHORT).show();
            return Result.failure();
        }
    }
}
