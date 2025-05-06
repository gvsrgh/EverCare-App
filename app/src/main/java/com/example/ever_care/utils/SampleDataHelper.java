package com.example.ever_care.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.ever_care.models.HealthData;
import com.example.ever_care.models.Medication;
import com.example.ever_care.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SampleDataHelper {
    private static final String TAG = "SampleDataHelper";
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    public interface SampleDataCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public SampleDataHelper() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void addSampleData(final SampleDataCallback callback) {
        // Create sample elderly user
        createSampleElderlyUser(new SampleDataCallback() {
            @Override
            public void onSuccess() {
                // Create sample family member
                createSampleFamilyMember(new SampleDataCallback() {
                    @Override
                    public void onSuccess() {
                        // Add sample medications
                        addSampleMedications(new SampleDataCallback() {
                            @Override
                            public void onSuccess() {
                                // Add sample health data
                                addSampleHealthData(new SampleDataCallback() {
                                    @Override
                                    public void onSuccess() {
                                        // Add sample daily check-in
                                        addSampleDailyCheckIn(callback);
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        callback.onError("Error adding health data: " + errorMessage);
                                    }
                                });
                            }

                            @Override
                            public void onError(String errorMessage) {
                                callback.onError("Error adding medications: " + errorMessage);
                            }
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        callback.onError("Error creating family member: " + errorMessage);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError("Error creating elderly user: " + errorMessage);
            }
        });
    }

    private void createSampleElderlyUser(final SampleDataCallback callback) {
        // First, create the user in Firebase Auth
        mAuth.createUserWithEmailAndPassword("elderly@example.com", "password123")
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String userId = task.getResult().getUser().getUid();

                            // Create user object
                            User user = new User();
                            user.setId(userId);
                            user.setFullName("John Doe");
                            user.setEmail("elderly@example.com");
                            user.setAge(75);
                            user.setGender("Male");
                            user.setElderly(true);
                            user.setPhoneNumber("1234567890");

                            // Save to database
                            mDatabase.child("users").child(userId).setValue(user)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "Sample elderly user created");
                                                callback.onSuccess();
                                            } else {
                                                callback.onError(task.getException().getMessage());
                                            }
                                        }
                                    });
                        } else {
                            callback.onError(task.getException().getMessage());
                        }
                    }
                });
    }

    private void createSampleFamilyMember(final SampleDataCallback callback) {
        // First, create the user in Firebase Auth
        mAuth.createUserWithEmailAndPassword("family@example.com", "password123")
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String userId = task.getResult().getUser().getUid();

                            // Create user object
                            User user = new User();
                            user.setId(userId);
                            user.setFullName("Jane Smith");
                            user.setEmail("family@example.com");
                            user.setAge(45);
                            user.setGender("Female");
                            user.setElderly(false);
                            user.setPhoneNumber("9876543210");
                            user.setLinkedUserId("ELDERLY_USER_ID"); // Replace with actual elderly user ID

                            // Save to database
                            mDatabase.child("users").child(userId).setValue(user)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "Sample family member created");
                                                callback.onSuccess();
                                            } else {
                                                callback.onError(task.getException().getMessage());
                                            }
                                        }
                                    });
                        } else {
                            callback.onError(task.getException().getMessage());
                        }
                    }
                });
    }

    private void addSampleMedications(final SampleDataCallback callback) {
        String elderlyId = "ELDERLY_USER_ID"; // Replace with actual elderly user ID

        // Create medication 1
        String medicationId1 = mDatabase.child("medications").push().getKey();
        Medication medication1 = new Medication(
                medicationId1,
                "Aspirin",
                "100mg",
                "Once daily",
                new Date(),
                getDatePlusMonths(1),
                "08:00 AM",
                elderlyId,
                elderlyId
        );

        // Create medication 2
        String medicationId2 = mDatabase.child("medications").push().getKey();
        Medication medication2 = new Medication(
                medicationId2,
                "Vitamin D",
                "1000 IU",
                "Once daily",
                new Date(),
                getDatePlusMonths(3),
                "09:00 AM",
                elderlyId,
                elderlyId
        );

        // Save medications
        Map<String, Object> medicationsUpdates = new HashMap<>();
        medicationsUpdates.put("/medications/" + medicationId1, medication1);
        medicationsUpdates.put("/medications/" + medicationId2, medication2);

        mDatabase.updateChildren(medicationsUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Sample medications added");
                            callback.onSuccess();
                        } else {
                            callback.onError(task.getException().getMessage());
                        }
                    }
                });
    }

    private void addSampleHealthData(final SampleDataCallback callback) {
        String elderlyId = "ELDERLY_USER_ID"; // Replace with actual elderly user ID

        // Create health data 1 (today)
        String healthDataId1 = mDatabase.child("healthData").push().getKey();
        HealthData healthData1 = new HealthData(
                healthDataId1,
                98.6f,
                "120/80",
                95.0f,
                new Date(),
                elderlyId
        );

        // Create health data 2 (yesterday)
        String healthDataId2 = mDatabase.child("healthData").push().getKey();
        HealthData healthData2 = new HealthData(
                healthDataId2,
                99.1f,
                "125/85",
                100.0f,
                getDateMinusDays(1),
                elderlyId
        );

        // Save health data
        Map<String, Object> healthDataUpdates = new HashMap<>();
        healthDataUpdates.put("/healthData/" + healthDataId1, healthData1);
        healthDataUpdates.put("/healthData/" + healthDataId2, healthData2);

        mDatabase.updateChildren(healthDataUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Sample health data added");
                            callback.onSuccess();
                        } else {
                            callback.onError(task.getException().getMessage());
                        }
                    }
                });
    }

    private void addSampleDailyCheckIn(final SampleDataCallback callback) {
        String elderlyId = "ELDERLY_USER_ID"; // Replace with actual elderly user ID

        // Create daily check-in
        String checkInId = mDatabase.child("dailyCheckIns").push().getKey();
        Map<String, Object> checkIn = new HashMap<>();
        checkIn.put("elderlyId", elderlyId);
        checkIn.put("feeling", "Good");
        checkIn.put("timestamp", new Date().getTime());

        mDatabase.child("dailyCheckIns").child(checkInId).setValue(checkIn)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Sample daily check-in added");
                            callback.onSuccess();
                        } else {
                            callback.onError(task.getException().getMessage());
                        }
                    }
                });
    }

    private Date getDatePlusMonths(int months) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, months);
        return calendar.getTime();
    }

    private Date getDateMinusDays(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -days);
        return calendar.getTime();
    }
}