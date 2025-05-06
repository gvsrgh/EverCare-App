package com.example.ever_care;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;

public class ManageProductApplication extends Application {
    public static final String MEDICATION_CHANNEL_ID = "medication_channel";
    public static final String EMERGENCY_CHANNEL_ID = "emergency_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Firebase
        FirebaseDatabase.getInstance().setLogLevel(Logger.Level.DEBUG);
        FirebaseApp.initializeApp(this);

        // Create notification channels
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Medication channel
            NotificationChannel medicationChannel = new NotificationChannel(
                    MEDICATION_CHANNEL_ID,
                    "Medication Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            medicationChannel.setDescription("Notifications for medication reminders");

            // Emergency channel
            NotificationChannel emergencyChannel = new NotificationChannel(
                    EMERGENCY_CHANNEL_ID,
                    "Emergency Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            emergencyChannel.setDescription("Emergency alerts for family members");
            emergencyChannel.enableVibration(true);
            emergencyChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(medicationChannel);
            manager.createNotificationChannel(emergencyChannel);
        }
    }
}