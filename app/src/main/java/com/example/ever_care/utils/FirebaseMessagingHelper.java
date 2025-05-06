package com.example.ever_care.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

public class FirebaseMessagingHelper {
    private static final String TAG = "FirebaseMessagingHelper";

    public void subscribeToTopic(String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "Failed to subscribe to topic: " + topic);
                        } else {
                            Log.d(TAG, "Subscribed to topic: " + topic);
                        }
                    }
                });
    }

    public void unsubscribeFromTopic(String topic) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "Failed to unsubscribe from topic: " + topic);
                        } else {
                            Log.d(TAG, "Unsubscribed from topic: " + topic);
                        }
                    }
                });
    }

    public void sendMedicationReminder(String topic, String medicationName) {
        // In a real app, you would use Firebase Cloud Functions or your own server
        // to send notifications. For this demo, we'll just log the intent.
        Log.d(TAG, "Sending medication reminder for " + medicationName + " to topic: " + topic);
    }

    public void sendMissedMedicationAlert(String topic, String elderlyName, String medicationName) {
        // In a real app, you would use Firebase Cloud Functions or your own server
        // to send notifications. For this demo, we'll just log the intent.
        Log.d(TAG, "Sending missed medication alert for " + elderlyName + " (" + medicationName + ") to topic: " + topic);
    }

    public void sendEmergencyNotification(String topic, String title, String message) {
        // In a real app, you would use Firebase Cloud Functions or your own server
        // to send notifications. For this demo, we'll just log the intent.
        Log.d(TAG, "Sending emergency notification to topic: " + topic + ", Title: " + title + ", Message: " + message);
    }
}