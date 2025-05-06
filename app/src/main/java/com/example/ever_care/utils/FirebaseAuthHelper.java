package com.example.ever_care.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.example.ever_care.models.User;

public class FirebaseAuthHelper {
    private static final String TAG = "FirebaseAuthHelper";
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseMessagingHelper messagingHelper;

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onError(String errorMessage);
    }

    public FirebaseAuthHelper() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        messagingHelper = new FirebaseMessagingHelper();
    }

    public void registerUser(final String email, String password, final User userData, final AuthCallback callback) {
        Log.d(TAG, "Starting user registration for: " + email);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            Log.d(TAG, "User created with ID: " + firebaseUser.getUid());

                            userData.setId(firebaseUser.getUid());

                            // Log the user data for debugging
                            Log.d(TAG, "Saving user data: " + userData.getFullName() + ", " + userData.getEmail());

                            // Save user data to database
                            mDatabase.child("users").child(firebaseUser.getUid()).setValue(userData)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "User data saved successfully");

                                                // Subscribe to topics based on user type
                                                if (userData.isElderly()) {
                                                    messagingHelper.subscribeToTopic("elderly_" + firebaseUser.getUid());
                                                } else {
                                                    messagingHelper.subscribeToTopic("family_" + userData.getLinkedUserId());
                                                }

                                                callback.onSuccess(firebaseUser);
                                            } else {
                                                Log.e(TAG, "Failed to save user data", task.getException());
                                                callback.onError("Failed to save user data: " + task.getException().getMessage());
                                            }
                                        }
                                    });
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            callback.onError(task.getException().getMessage());
                        }
                    }
                });
    }

    public void loginUser(String email, String password, final AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            callback.onSuccess(user);
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            callback.onError(task.getException().getMessage());
                        }
                    }
                });
    }

    public void logoutUser() {
        mAuth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }
}