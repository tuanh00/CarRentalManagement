package com.example.carrentalapp.uiactivities.customer;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;

import androidx.annotation.NonNull;

import com.example.carrentalapp.R;

public class RatingDialog extends Dialog {

    private RatingBar ratingBar;
    private Button submitButton;
    private OnRatingSubmittedListener listener;

    public RatingDialog(@NonNull Context context, OnRatingSubmittedListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_rating);

        ratingBar = findViewById(R.id.ratingBar);
        submitButton = findViewById(R.id.submitRatingButton);

        submitButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRatingSubmitted((int) ratingBar.getRating());
            }
            dismiss();
        });
    }

    public interface OnRatingSubmittedListener {
        void onRatingSubmitted(int rating);
    }
}
