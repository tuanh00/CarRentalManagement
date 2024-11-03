package com.example.carrentalapp.auth;

import android.app.Activity;
import android.content.Intent;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;

public class SignOutActivity {
    public static void signOut(Activity activity, FirebaseAuth auth, GoogleSignInClient googleSignInClient) {
        //sign-out firebase
        auth.signOut();

        //sign out google provider
        googleSignInClient.signOut().addOnCompleteListener(activity, task -> {
            Intent intent = new Intent(activity, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            activity.finish();
        });
    }
}
