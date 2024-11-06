package com.example.carrentalapp;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class FirebaseConfig extends Application {
    public void onCreate() {
        super.onCreate();

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApiKey("AIzaSyBdfg_G19mw5tWqsHupVOb56gLvOl5cLeY")
                .setApplicationId("1:655767929093:android:62d9fdc09cc7a93f17376c")
                .setDatabaseUrl("https://carrentalmanagment-default-rtdb.firebaseio.com")
                .setProjectId("carrentalmanagment")
                .setStorageBucket("carrentalmanagment.appspot.com")
                .build();

        FirebaseApp.initializeApp(this, options);
        Log.d("FirebaseInit", "Firebase initialized in CarRentalAppApplication");
    }
}
