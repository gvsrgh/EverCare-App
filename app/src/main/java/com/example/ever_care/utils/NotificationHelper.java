package com.example.ever_care.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.ever_care.ManageProductApplication;
import com.example.ever_care.R;
import com.example.ever_care.activities.ElderlyDashboardActivity;
import com.example.ever_care.activities.FamilyDashboardActivity;
import com.example.ever_care.models.User;

import java.util.List;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";

    private Context context;
    private FirebaseDatabaseHelper databaseHelper;

    public NotificationHelper(Context context) {
        this.context = context;
        this.databaseHelper = new FirebaseDatabaseHelper();
    }

    public void sendMedicationReminder(String medicationName, String elderlyId, String medicationId) {
        Intent intent = new Intent(context, ElderlyDashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, ManageProductApplication.MEDICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Medication Reminder")
                        .setContentText("It's time to take your " + medicationName)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify(medicationId.hashCode(), notificationBuilder.build());
        } catch (SecurityException e) {
            Log.e(TAG, "Error sending notification: " + e.getMessage());
        }
    }

    public void sendMissedMedicationAlert(String medicationName, String elderlyId, String medicationId) {
        // Get the elderly user's information
        databaseHelper.getUserById(elderlyId, new FirebaseDatabaseHelper.DataCallback<User>() {
            @Override
            public void onSuccess(User elderly) {
                // Find all family members linked to this elderly person
                databaseHelper.getFamilyMembersForElderly(elderlyId, new FirebaseDatabaseHelper.DataCallback<List<User>>() {
                    @Override
                    public void onSuccess(List<User> familyMembers) {
                        for (User familyMember : familyMembers) {
                            // Send notification to each family member
                            sendMissedMedicationNotification(
                                    familyMember.getId(),
                                    elderly.getFullName(),
                                    medicationName,
                                    medicationId
                            );
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Error finding family members: " + errorMessage);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error getting elderly user: " + errorMessage);
            }
        });
    }

    private void sendMissedMedicationNotification(String familyMemberId, String elderlyName, String medicationName, String medicationId) {
        Intent intent = new Intent(context, FamilyDashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, ManageProductApplication.MEDICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Missed Medication Alert")
                        .setContentText(elderlyName + " missed their " + medicationName + " medication")
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            // Use a unique ID for each family member and medication combination
            notificationManager.notify(
                    (familyMemberId + medicationId).hashCode(),
                    notificationBuilder.build()
            );
        } catch (SecurityException e) {
            Log.e(TAG, "Error sending notification: " + e.getMessage());
        }
    }

    public void sendEmergencyAlert(String elderlyId, String elderlyName) {
        // Find all family members linked to this elderly person
        databaseHelper.getFamilyMembersForElderly(elderlyId, new FirebaseDatabaseHelper.DataCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> familyMembers) {
                for (User familyMember : familyMembers) {
                    // Send emergency notification to each family member
                    sendEmergencyNotification(
                            familyMember.getId(),
                            elderlyName
                    );
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error finding family members: " + errorMessage);
            }
        });
    }

    private void sendEmergencyNotification(String familyMemberId, String elderlyName) {
        Intent intent = new Intent(context, FamilyDashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmSound == null) {
            alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        }

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, ManageProductApplication.EMERGENCY_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("EMERGENCY ALERT")
                        .setContentText(elderlyName + " is not feeling well and may need assistance")
                        .setAutoCancel(true)
                        .setSound(alarmSound)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setVibrate(new long[]{0, 1000, 500, 1000})
                        .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            // Use a unique ID for each family member
            notificationManager.notify(
                    ("emergency_" + familyMemberId).hashCode(),
                    notificationBuilder.build()
            );
        } catch (SecurityException e) {
            Log.e(TAG, "Error sending notification: " + e.getMessage());
        }
    }
}