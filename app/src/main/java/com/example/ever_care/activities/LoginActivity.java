package com.example.ever_care.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ever_care.R;
import com.example.ever_care.models.User;
import com.example.ever_care.utils.FirebaseAuthHelper;
import com.example.ever_care.utils.FirebaseDatabaseHelper;
import com.example.ever_care.utils.PreferenceManager;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister;
    private ProgressBar progressBar;
    private PreferenceManager preferenceManager;
    private FirebaseAuthHelper authHelper;
    private FirebaseDatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        preferenceManager = new PreferenceManager(getApplicationContext());
        authHelper = new FirebaseAuthHelper();
        databaseHelper = new FirebaseDatabaseHelper();

        // Initialize views
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister);
        progressBar = findViewById(R.id.progressBar);

        // Set click listeners
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    loginUser();
                }
            }
        });

        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private boolean validateInputs() {
        if (TextUtils.isEmpty(editTextEmail.getText().toString().trim())) {
            editTextEmail.setError("Email is required");
            return false;
        }

        if (TextUtils.isEmpty(editTextPassword.getText().toString().trim())) {
            editTextPassword.setError("Password is required");
            return false;
        }

        return true;
    }

    private void loginUser() {
        progressBar.setVisibility(View.VISIBLE);
        buttonLogin.setEnabled(false);

        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        authHelper.loginUser(email, password, new FirebaseAuthHelper.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                // Get user data from database
                databaseHelper.getUserById(user.getUid(), new FirebaseDatabaseHelper.DataCallback<User>() {
                    @Override
                    public void onSuccess(User userData) {
                        // Save user session
                        preferenceManager.saveUserSession(
                                userData.getId(),
                                userData.getFullName(),
                                userData.getEmail(),
                                userData.isElderly()
                        );

                        // If family member, save linked elderly user ID
                        if (!userData.isElderly() && userData.getLinkedUserId() != null) {
                            preferenceManager.saveLinkedUserId(userData.getLinkedUserId());
                        }

                        // Navigate to appropriate dashboard
                        if (userData.isElderly()) {
                            startActivity(new Intent(LoginActivity.this, ElderlyDashboardActivity.class));
                        } else {
                            startActivity(new Intent(LoginActivity.this, FamilyDashboardActivity.class));
                        }

                        finish();
                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        progressBar.setVisibility(View.GONE);
                        buttonLogin.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                buttonLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Login failed: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}