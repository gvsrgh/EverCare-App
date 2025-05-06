package com.example.ever_care.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import com.example.ever_care.models.HealthData;
import com.example.ever_care.models.Medication;
import com.example.ever_care.models.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseDatabaseHelper {
    private static final String TAG = "FirebaseDatabaseHelper";
    private DatabaseReference mDatabase;
    private FirebaseMessagingHelper messagingHelper;

    public interface DataCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }

    public FirebaseDatabaseHelper() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        messagingHelper = new FirebaseMessagingHelper();
    }

    // User methods
    public void getUserById(String userId, final DataCallback<User> callback) {
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    callback.onSuccess(user);
                } else {
                    callback.onError("User not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    public void getUserByEmail(String email, final DataCallback<User> callback) {
        Query query = mDatabase.child("users").orderByChild("email").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        callback.onSuccess(user);
                        return;
                    }
                }
                callback.onError("User not found");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    public void updateUserPhoneNumber(String userId, String phoneNumber, final DataCallback<Boolean> callback) {
        mDatabase.child("users").child(userId).child("phoneNumber").setValue(phoneNumber)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(true);
                        } else {
                            callback.onError(task.getException().getMessage());
                        }
                    }
                });
    }

    // Medication methods
    // In FirebaseDatabaseHelper.java, update the saveMedication method:

    public void saveMedication(Medication medication, final DataCallback<Boolean> callback) {
        if (medication.getId() == null) {
            medication.setId(mDatabase.child("medications").push().getKey());
        }

        Log.d(TAG, "Saving medication: " + medication.getName() + " with ID: " + medication.getId());

        mDatabase.child("medications").child(medication.getId()).setValue(medication)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Medication saved successfully");
                            callback.onSuccess(true);
                        } else {
                            Log.e(TAG, "Failed to save medication", task.getException());
                            callback.onError(task.getException() != null ?
                                    task.getException().getMessage() : "Unknown error");
                        }
                    }
                });
    }

    public void getMedicationsForElderly(String elderlyId, final DataCallback<List<Medication>> callback) {
        Query query = mDatabase.child("medications").orderByChild("elderlyId").equalTo(elderlyId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Medication> medications = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Medication medication = snapshot.getValue(Medication.class);
                    medications.add(medication);
                }
                callback.onSuccess(medications);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    public void updateMedicationStatus(String medicationId, boolean taken, Date takenTime, final DataCallback<Boolean> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("taken", taken);
        updates.put("lastTakenTime", takenTime);

        mDatabase.child("medications").child(medicationId).updateChildren(updates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(true);
                        } else {
                            callback.onError(task.getException().getMessage());
                        }
                    }
                });
    }

    // Health data methods
    public void saveHealthData(HealthData healthData, final DataCallback<Boolean> callback) {
        if (healthData.getId() == null) {
            healthData.setId(mDatabase.child("healthData").push().getKey());
        }

        mDatabase.child("healthData").child(healthData.getId()).setValue(healthData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(true);
                        } else {
                            callback.onError(task.getException().getMessage());
                        }
                    }
                });
    }

    public void getHealthDataForElderly(String elderlyId, final DataCallback<List<HealthData>> callback) {
        Query query = mDatabase.child("healthData").orderByChild("elderlyId").equalTo(elderlyId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<HealthData> healthDataList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    HealthData healthData = snapshot.getValue(HealthData.class);
                    healthDataList.add(healthData);
                }
                callback.onSuccess(healthDataList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    // Daily check-in methods
    public void saveDailyCheckIn(String elderlyId, String feeling, final DataCallback<Boolean> callback) {
        Map<String, Object> checkIn = new HashMap<>();
        checkIn.put("elderlyId", elderlyId);
        checkIn.put("feeling", feeling);
        checkIn.put("timestamp", new Date().getTime());

        String checkInId = mDatabase.child("dailyCheckIns").push().getKey();
        mDatabase.child("dailyCheckIns").child(checkInId).setValue(checkIn)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // If feeling is "Not Well", send emergency notification to family members
                            if (feeling.equals("Not Well")) {
                                sendEmergencyAlert(elderlyId);
                            }
                            callback.onSuccess(true);
                        } else {
                            callback.onError(task.getException().getMessage());
                        }
                    }
                });
    }

    public void getLatestDailyCheckIn(String elderlyId, final DataCallback<Map<String, Object>> callback) {
        Query query = mDatabase.child("dailyCheckIns")
                .orderByChild("elderlyId")
                .equalTo(elderlyId)
                .limitToLast(1);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Map<String, Object> checkIn = (Map<String, Object>) snapshot.getValue();
                        callback.onSuccess(checkIn);
                        return;
                    }
                }
                callback.onError("No check-in found");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    // Helper methods
    private void sendEmergencyAlert(String elderlyId) {
        // Get the elderly user's information
        getUserById(elderlyId, new DataCallback<User>() {
            @Override
            public void onSuccess(User elderly) {
                // Find all family members linked to this elderly person
                Query query = mDatabase.child("users")
                        .orderByChild("linkedUserId")
                        .equalTo(elderlyId);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            User familyMember = snapshot.getValue(User.class);
                            // Send emergency notification to each family member
                            messagingHelper.sendEmergencyNotification(
                                    "family_" + elderlyId,
                                    "Emergency Alert",
                                    elderly.getFullName() + " is not feeling well and may need assistance."
                            );
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Error finding family members: " + databaseError.getMessage());
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error getting elderly user: " + errorMessage);
            }
        });
    }
    public void getMedicationById(String medicationId, final DataCallback<Medication> callback) {
        mDatabase.child("medications").child(medicationId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Medication medication = dataSnapshot.getValue(Medication.class);
                    callback.onSuccess(medication);
                } else {
                    callback.onError("Medication not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    public void getFamilyMembersForElderly(String elderlyId, final DataCallback<List<User>> callback) {
        Query query = mDatabase.child("users")
                .orderByChild("linkedUserId")
                .equalTo(elderlyId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<User> familyMembers = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    familyMembers.add(user);
                }
                callback.onSuccess(familyMembers);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }
}