package com.example.ever_care.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ever_care.R;
import com.example.ever_care.models.User;
import com.example.ever_care.utils.FirebaseAuthHelper;
import com.example.ever_care.utils.FirebaseDatabaseHelper;
import com.example.ever_care.utils.PreferenceManager;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextFullName, editTextEmail, editTextPassword, editTextConfirmPassword;
    private EditText editTextAge, editTextElderlyEmail, editTextPhoneNumber;
    private RadioGroup radioGroupGender, radioGroupUserType;
    private RadioButton radioButtonMale, radioButtonFemale, radioButtonElderly, radioButtonFamilyMember;
    private Button buttonRegister;
    private TextView textViewLogin;
    private LinearLayout layoutElderlyEmail;
    private ProgressBar progressBar;

    private PreferenceManager preferenceManager;
    private FirebaseAuthHelper authHelper;
    private FirebaseDatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        preferenceManager = new PreferenceManager(getApplicationContext());
        authHelper = new FirebaseAuthHelper();
        databaseHelper = new FirebaseDatabaseHelper();

        // Initialize views
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextAge = findViewById(R.id.editTextAge);
        editTextElderlyEmail = findViewById(R.id.editTextElderlyEmail);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);

        radioGroupGender = findViewById(R.id.radioGroupGender);
        radioButtonMale = findViewById(R.id.radioButtonMale);
        radioButtonFemale = findViewById(R.id.radioButtonFemale);

        radioGroupUserType = findViewById(R.id.radioGroupUserType);
        radioButtonElderly = findViewById(R.id.radioButtonElderly);
        radioButtonFamilyMember = findViewById(R.id.radioButtonFamilyMember);

        layoutElderlyEmail = findViewById(R.id.layoutElderlyEmail);

        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLogin = findViewById(R.id.textViewLogin);
        progressBar = findViewById(R.id.progressBar);

        // Set click listeners
        radioGroupUserType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioButtonFamilyMember) {
                    layoutElderlyEmail.setVisibility(View.VISIBLE);
                } else {
                    layoutElderlyEmail.setVisibility(View.GONE);
                }
            }
        });

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    registerUser();
                }
            }
        });

        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to login screen
            }
        });
    }

    private boolean validateInputs() {
        if (TextUtils.isEmpty(editTextFullName.getText().toString().trim())) {
            editTextFullName.setError("Full name is required");
            return false;
        }

        if (TextUtils.isEmpty(editTextEmail.getText().toString().trim())) {
            editTextEmail.setError("Email is required");
            return false;
        }

        if (TextUtils.isEmpty(editTextPassword.getText().toString().trim())) {
            editTextPassword.setError("Password is required");
            return false;
        }

        if (TextUtils.isEmpty(editTextConfirmPassword.getText().toString().trim())) {
            editTextConfirmPassword.setError("Confirm password is required");
            return false;
        }

        if (!editTextPassword.getText().toString().equals(editTextConfirmPassword.getText().toString())) {
            editTextConfirmPassword.setError("Passwords do not match");
            return false;
        }

        if (TextUtils.isEmpty(editTextAge.getText().toString().trim())) {
            editTextAge.setError("Age is required");
            return false;
        }

        if (TextUtils.isEmpty(editTextPhoneNumber.getText().toString().trim())) {
            editTextPhoneNumber.setError("Phone number is required");
            return false;
        }

        if (radioGroupGender.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (radioGroupUserType.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select user type", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (radioButtonFamilyMember.isChecked() && TextUtils.isEmpty(editTextElderlyEmail.getText().toString().trim())) {
            editTextElderlyEmail.setError("Elderly person's email is required");
            return false;
        }

        return true;
    }

    private void registerUser() {
        progressBar.setVisibility(View.VISIBLE);
        buttonRegister.setEnabled(false);

        final String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        final String fullName = editTextFullName.getText().toString().trim();
        final int age = Integer.parseInt(editTextAge.getText().toString().trim());
        final String gender = radioButtonMale.isChecked() ? "Male" : "Female";
        final boolean isElderly = radioButtonElderly.isChecked();
        final String phoneNumber = editTextPhoneNumber.getText().toString().trim();

        // Create user object
        final User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setAge(age);
        user.setGender(gender);
        user.setElderly(isElderly);
        user.setPhoneNumber(phoneNumber);

        if (!isElderly) {
            // If family member, get the elderly person's email
            final String elderlyEmail = editTextElderlyEmail.getText().toString().trim();

            // Find the elderly user by email
            databaseHelper.getUserByEmail(elderlyEmail, new FirebaseDatabaseHelper.DataCallback<User>() {
                @Override
                public void onSuccess(User elderlyUser) {
                    // Link family member to elderly user
                    user.setLinkedUserId(elderlyUser.getId());

                    // Register the user
                    registerWithFirebase(email, password, user);
                }

                @Override
                public void onError(String errorMessage) {
                    progressBar.setVisibility(View.GONE);
                    buttonRegister.setEnabled(true);
                    Toast.makeText(RegisterActivity.this, "Elderly user not found: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Register elderly user directly
            registerWithFirebase(email, password, user);
        }
    }

    private void registerWithFirebase(String email, String password, final User user) {
        authHelper.registerUser(email, password, user, new FirebaseAuthHelper.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                // Save user session
                preferenceManager.saveUserSession(
                        user.getId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.isElderly()
                );

                // If family member, save linked elderly user ID
                if (!user.isElderly() && user.getLinkedUserId() != null) {
                    preferenceManager.saveLinkedUserId(user.getLinkedUserId());
                }

                // Navigate to appropriate dashboard
                if (user.isElderly()) {
                    startActivity(new Intent(RegisterActivity.this, ElderlyDashboardActivity.class));
                } else {
                    startActivity(new Intent(RegisterActivity.this, FamilyDashboardActivity.class));
                }

                finish();
                Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                buttonRegister.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "Registration failed: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}