package com.example.carrentalapp.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.carrentalapp.R;
import com.example.carrentalapp.utilities.LoginActivity;

public class Splash extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Find the logo ImageView by its ID
        ImageView logo = findViewById(R.id.splash_logo);

        // Load the fade-in animation
        Animation fadeIn = AnimationUtils.loadAnimation(this, com.stripe.android.R.anim.abc_fade_in);

        // Apply the fade-in animation to the logo
        logo.startAnimation(fadeIn);
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(Splash.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_TIME_OUT);

    }
}