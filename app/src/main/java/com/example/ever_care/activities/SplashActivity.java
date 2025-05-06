package com.example.ever_care.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ever_care.R;
import com.example.ever_care.utils.PreferenceManager;
import com.example.ever_care.utils.SampleDataHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIMEOUT = 2000; // 2 seconds
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        preferenceManager = new PreferenceManager(getApplicationContext());

        // Check Firebase connection
        checkFirebaseConnection();

        // For testing only: Add sample data
        // Uncomment this when you want to add sample data
        // addSampleData();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (preferenceManager.isLoggedIn()) {
                    // User is already logged in, check if elderly or family member
                    if (preferenceManager.isElderly()) {
                        startActivity(new Intent(SplashActivity.this, ElderlyDashboardActivity.class));
                    } else {
                        startActivity(new Intent(SplashActivity.this, FamilyDashboardActivity.class));
                    }
                } else {
                    // User is not logged in, go to login screen
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
                finish();
            }
        }, SPLASH_TIMEOUT);
    }

    private void checkFirebaseConnection() {
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    Log.d("Firebase", "Connected to Firebase");
                } else {
                    Log.d("Firebase", "Not connected to Firebase");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Listener was cancelled");
            }
        });
    }

    private void addSampleData() {
        SampleDataHelper sampleDataHelper = new SampleDataHelper();
        sampleDataHelper.addSampleData(new SampleDataHelper.SampleDataCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(SplashActivity.this, "Sample data added successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(SplashActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}