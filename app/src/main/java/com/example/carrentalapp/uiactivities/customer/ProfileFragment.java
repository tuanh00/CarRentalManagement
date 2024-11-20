package com.example.carrentalapp.uiactivities.customer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.carrentalapp.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; // Added for Change Avatar button
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private TextView textViewUserName, textViewUserEmail, textViewMemberSince;
    private ImageView imageViewAvatar;
    private Button buttonChangeAvatar;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        textViewUserName = view.findViewById(R.id.textViewUserName);
        textViewUserEmail = view.findViewById(R.id.textViewUserEmail);
        textViewMemberSince = view.findViewById(R.id.textViewMemberSince);
        imageViewAvatar = view.findViewById(R.id.imageViewAvatar);
        buttonChangeAvatar = view.findViewById(R.id.buttonChangeAvatar);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        fetchUserInfo();

        buttonChangeAvatar.setOnClickListener(v -> openFileChooser());

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
        String imgUrl = sharedPreferences.getString("imgUrl", "");

        long createdAtMillis = sharedPreferences.getLong("createdAt", 0);
        String memberSince = "N/A";
        if (createdAtMillis != 0) {
            Date createdAtDate = new Date(createdAtMillis);
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            memberSince = sdf.format(createdAtDate);
        }

        textViewUserName.setText(userName);
        textViewUserEmail.setText(userEmail);
        textViewMemberSince.setText(memberSince);

        // Load avatar image using Glide
        if (!TextUtils.isEmpty(imgUrl)) {
            Glide.with(this)
                    .load(imgUrl)
                    .placeholder(R.drawable.ic_user_avatar_placeholder)
                    .error(R.drawable.ic_user_avatar_placeholder)
                    .into(imageViewAvatar);
        } else {
            imageViewAvatar.setImageResource(R.drawable.ic_user_avatar_placeholder);
        }
    }

    /**
     * Opens the image chooser to select a new avatar.
     */
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


    /**
     * Handles the result from the image chooser.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if the result is from image picker and is successful
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadImageToFirebase(imageUri);
        }
    }

    /**
     * Uploads the selected image to Firebase Storage and updates Firestore.
     */
    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri != null) {
            // Create a reference to 'avatars/userid.jpg'
            String userId = getActivity().getSharedPreferences("CarRentalAppPrefs", Context.MODE_PRIVATE)
                    .getString("user_id", "");
            if (TextUtils.isEmpty(userId)) {
                Toast.makeText(getContext(), "User ID not found.", Toast.LENGTH_SHORT).show();
                return;
            }
            StorageReference fileRef = storageReference.child("avatars/" + userId + ".jpg");

            // Show a loading message
            Toast.makeText(getContext(), "Uploading avatar...", Toast.LENGTH_SHORT).show();

            // Upload the image
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get the download URL
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();
                            updateUserAvatar(downloadUrl);
                        }).addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to retrieve download URL.", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to upload avatar.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    });
        }
    }

    /**
     * Updates the user's avatar URL in Firestore and SharedPreferences.
     */
    private void updateUserAvatar(String downloadUrl) {
        String userId = getActivity().getSharedPreferences("CarRentalAppPrefs", Context.MODE_PRIVATE)
                .getString("user_id", "");

        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(getContext(), "User ID not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update Firestore
        db.collection("Users").document(userId)
                .update("imgUrl", downloadUrl)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Avatar updated successfully.", Toast.LENGTH_SHORT).show();

                    // Update SharedPreferences
                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("CarRentalAppPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("imgUrl", downloadUrl);
                    editor.apply();

                    // Update ImageView
                    Glide.with(this)
                            .load(downloadUrl)
                            .placeholder(R.drawable.ic_user_avatar_placeholder)
                            .error(R.drawable.ic_user_avatar_placeholder)
                            .into(imageViewAvatar);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to update avatar in database.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }
}
