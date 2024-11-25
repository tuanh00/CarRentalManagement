// ProfileFragment.java
package com.example.carrentalapp.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.carrentalapp.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ProfileFragment extends Fragment {

    private TextView textViewUserName, textViewUserEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        textViewUserName = view.findViewById(R.id.textViewUserName);
        textViewUserEmail = view.findViewById(R.id.textViewUserEmail);

        fetchUserInfo();

        return view;
    }

    /**
     * Fetches user information from SharedPreferences and displays it.
     */
    private void fetchUserInfo() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("CarRentalAppPrefs", Context.MODE_PRIVATE);
        String firstName = sharedPreferences.getString("first_name", "N/A");
        String lastName = sharedPreferences.getString("last_name", "N/A");
        String userName = firstName + " " + lastName;
        String userEmail = sharedPreferences.getString("email", "N/A");

        textViewUserName.setText(userName);
        textViewUserEmail.setText(userEmail);
    }
}
