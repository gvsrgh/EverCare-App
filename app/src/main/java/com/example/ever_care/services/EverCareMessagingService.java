package com.example.ever_care.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import com.example.ever_care.ManageProductApplication;
import com.example.ever_care.R;
import com.example.ever_care.activities.ElderlyDashboardActivity;
import com.example.ever_care.activities.FamilyDashboardActivity;

public class EverCareMessagingService extends FirebaseMessagingService {
    private static final String TAG = "EverCareMessagingService";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            String notificationType = remoteMessage.getData().get("type");
            if (notificationType != null) {
                switch (notificationType) {
                    case "medication_reminder":
                        handleMedicationReminder(remoteMessage);
                        break;
                    case "missed_medication":
                        handleMissedMedication(remoteMessage);
                        break;
                    case "emergency":
                        handleEmergency(remoteMessage);
                        break;
                    default:
                        // Handle other notification types
                        break;
                }
            }
        }

        // Check if message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            sendNotification(
                    remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody(),
                    ManageProductApplication.MEDICATION_CHANNEL_ID,
                    ElderlyDashboardActivity.class
            );
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);
        // Send the token to your server
    }

    private void handleMedicationReminder(RemoteMessage remoteMessage) {
        String title = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("message");

        sendNotification(
                title,
                message,
                ManageProductApplication.MEDICATION_CHANNEL_ID,
                ElderlyDashboardActivity.class
        );
    }

    private void handleMissedMedication(RemoteMessage remoteMessage) {
        String title = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("message");

        sendNotification(
                title,
                message,
                ManageProductApplication.MEDICATION_CHANNEL_ID,
                FamilyDashboardActivity.class
        );
    }

    private void handleEmergency(RemoteMessage remoteMessage) {
        String title = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("message");

        sendEmergencyNotification(title, message);
    }

    private void sendNotification(String title, String messageBody, String channelId, Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

    private void sendEmergencyNotification(String title, String messageBody) {
        Intent intent = new Intent(this, FamilyDashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmSound == null) {
            alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        }

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, ManageProductApplication.EMERGENCY_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(alarmSound)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setVibrate(new long[]{0, 1000, 500, 1000})
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Use a unique ID for emergency notifications
        notificationManager.notify(911, notificationBuilder.build());
    }
}