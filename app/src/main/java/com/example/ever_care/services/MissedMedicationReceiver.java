package com.example.ever_care.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.ever_care.models.Medication;
import com.example.ever_care.utils.FirebaseDatabaseHelper;
import com.example.ever_care.utils.NotificationHelper;

public class MissedMedicationReceiver extends BroadcastReceiver {
    private static final String TAG = "MissedMedicationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String medicationId = intent.getStringExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_ID);
        String medicationName = intent.getStringExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_NAME);
        String elderlyId = intent.getStringExtra(MedicationAlarmReceiver.EXTRA_ELDERLY_ID);

        // Check if medication has been taken
        FirebaseDatabaseHelper databaseHelper = new FirebaseDatabaseHelper();
        databaseHelper.getMedicationById(medicationId, new FirebaseDatabaseHelper.DataCallback<Medication>() {
            @Override
            public void onSuccess(Medication medication) {
                if (!medication.isTaken()) {
                    // Medication was not taken, send notification to family members
                    NotificationHelper notificationHelper = new NotificationHelper(context);
                    notificationHelper.sendMissedMedicationAlert(medicationName, elderlyId, medicationId);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error checking medication status: " + errorMessage);
            }
        });
    }
}