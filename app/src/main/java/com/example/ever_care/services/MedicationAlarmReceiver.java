package com.example.ever_care.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.ever_care.models.Medication;
import com.example.ever_care.utils.NotificationHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MedicationAlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "MedicationAlarmReceiver";
    public static final String EXTRA_MEDICATION_ID = "medication_id";
    public static final String EXTRA_MEDICATION_NAME = "medication_name";
    public static final String EXTRA_ELDERLY_ID = "elderly_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        String medicationId = intent.getStringExtra(EXTRA_MEDICATION_ID);
        String medicationName = intent.getStringExtra(EXTRA_MEDICATION_NAME);
        String elderlyId = intent.getStringExtra(EXTRA_ELDERLY_ID);

        // Send notification
        NotificationHelper notificationHelper = new NotificationHelper(context);
        notificationHelper.sendMedicationReminder(medicationName, elderlyId, medicationId);

        // Schedule missed medication check
        scheduleMissedMedicationCheck(context, medicationId, medicationName, elderlyId);
    }

    public static void scheduleMedicationAlarm(Context context, Medication medication) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Parse reminder time
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        try {
            Date reminderTime = timeFormat.parse(medication.getReminderTime());
            calendar.setTime(reminderTime);

            // Set the date to today
            Calendar today = Calendar.getInstance();
            calendar.set(Calendar.YEAR, today.get(Calendar.YEAR));
            calendar.set(Calendar.MONTH, today.get(Calendar.MONTH));
            calendar.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH));

            // If the time has already passed today, schedule for tomorrow
            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            // Create intent for alarm
            Intent intent = new Intent(context, MedicationAlarmReceiver.class);
            intent.putExtra(EXTRA_MEDICATION_ID, medication.getId());
            intent.putExtra(EXTRA_MEDICATION_NAME, medication.getName());
            intent.putExtra(EXTRA_ELDERLY_ID, medication.getElderlyId());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    medication.getId().hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Schedule the alarm
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }

            Log.d(TAG, "Scheduled medication alarm for " + medication.getName() + " at " + timeFormat.format(calendar.getTime()));
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing reminder time: " + e.getMessage());
        }
    }

    private void scheduleMissedMedicationCheck(Context context, String medicationId, String medicationName, String elderlyId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Schedule check for 30 minutes later
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 30);

        // Create intent for missed medication check
        Intent intent = new Intent(context, MissedMedicationReceiver.class);
        intent.putExtra(EXTRA_MEDICATION_ID, medicationId);
        intent.putExtra(EXTRA_MEDICATION_NAME, medicationName);
        intent.putExtra(EXTRA_ELDERLY_ID, elderlyId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                ("missed_" + medicationId).hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Schedule the alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        }

        Log.d(TAG, "Scheduled missed medication check for " + medicationName + " at " + calendar.getTime());
    }
}