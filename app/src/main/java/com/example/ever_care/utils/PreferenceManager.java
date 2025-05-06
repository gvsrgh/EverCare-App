package com.example.ever_care.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class PreferenceManager {
    private SharedPreferences sharedPreferences;

    // Define all your keys here
    private static final String PREF_NAME = "EverCarePrefs";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_IS_ELDERLY = "isElderly";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn"; // Added this key
    private static final String KEY_LINKED_USER_ID = "linkedUserId";

    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveUserSession(String userId, String userName, String userEmail, boolean isElderly) {
        Log.d("PreferenceManager", "Saving user session: " + userId + ", " + userName + ", " + isElderly);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_USER_EMAIL, userEmail);
        editor.putBoolean(KEY_IS_ELDERLY, isElderly);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public void saveLinkedUserId(String linkedUserId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_LINKED_USER_ID, linkedUserId);
        editor.apply();
    }

    public String getUserId() {
        String userId = sharedPreferences.getString(KEY_USER_ID, null);
        Log.d("PreferenceManager", "Getting user ID: " + userId);
        return userId;
    }

    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, "");
    }

    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, "");
    }

    public boolean isElderly() {
        boolean isElderly = sharedPreferences.getBoolean(KEY_IS_ELDERLY, false);
        Log.d("PreferenceManager", "Is elderly: " + isElderly);
        return isElderly;
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getLinkedUserId() {
        return sharedPreferences.getString(KEY_LINKED_USER_ID, null);
    }

    public void clearSession() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}