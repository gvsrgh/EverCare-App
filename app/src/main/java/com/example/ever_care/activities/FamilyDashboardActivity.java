package com.example.ever_care.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ever_care.R;
import com.example.ever_care.adapters.MedicationAdapter;
import com.example.ever_care.models.HealthData;
import com.example.ever_care.models.Medication;
import com.example.ever_care.models.User;
import com.example.ever_care.utils.FirebaseAuthHelper;
import com.example.ever_care.utils.FirebaseDatabaseHelper;
import com.example.ever_care.utils.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FamilyDashboardActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView textViewWelcome, textViewElderlyName, textViewFeeling, textViewMedicationStatus;
    private TextView textViewLastCheckIn, textViewTemperature, textViewBloodPressure, textViewSugarLevel;
    private TextView textViewNoMedications, textViewNoHealthDataYet, textViewViewAllHealth;
    private Button buttonAddMedication;
    private RecyclerView recyclerViewMedications;

    private PreferenceManager preferenceManager;
    private FirebaseAuthHelper authHelper;
    private FirebaseDatabaseHelper databaseHelper;
    private MedicationAdapter medicationAdapter;
    private List<Medication> medicationList;
    private User elderlyUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_dashboard);

        preferenceManager = new PreferenceManager(getApplicationContext());
        authHelper = new FirebaseAuthHelper();
        databaseHelper = new FirebaseDatabaseHelper();

        // Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize views
        textViewWelcome = findViewById(R.id.textViewWelcome);
        textViewElderlyName = findViewById(R.id.textViewElderlyName);
        textViewFeeling = findViewById(R.id.textViewFeeling);
        textViewMedicationStatus = findViewById(R.id.textViewMedicationStatus);
        textViewLastCheckIn = findViewById(R.id.textViewLastCheckIn);
        textViewTemperature = findViewById(R.id.textViewTemperature);
        textViewBloodPressure = findViewById(R.id.textViewBloodPressure);
        textViewSugarLevel = findViewById(R.id.textViewSugarLevel);
        textViewNoMedications = findViewById(R.id.textViewNoMedications);
        textViewNoHealthDataYet = findViewById(R.id.textViewNoHealthDataYet);
        textViewViewAllHealth = findViewById(R.id.textViewViewAllHealth);

        buttonAddMedication = findViewById(R.id.buttonAddMedication);

        recyclerViewMedications = findViewById(R.id.recyclerViewMedications);

        // Set welcome message
        textViewWelcome.setText("Welcome, " + preferenceManager.getUserName() + "!");

        // Load elderly user data
        loadElderlyUserData();

        // Set up recycler view
        setupMedicationRecyclerView();

        // Set click listeners
        buttonAddMedication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FamilyDashboardActivity.this, MedicationDetailsActivity.class));
            }
        });

        textViewViewAllHealth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // In a real app, you would navigate to a health data history screen
                Toast.makeText(FamilyDashboardActivity.this, "View all health data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this screen
        loadMedications();
        loadHealthData();
        loadDailyStatus();
    }

    private void loadElderlyUserData() {
        String elderlyId = preferenceManager.getLinkedUserId();
        if (elderlyId != null) {
            databaseHelper.getUserById(elderlyId, new FirebaseDatabaseHelper.DataCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    elderlyUser = user;
                    textViewElderlyName.setText("Monitoring: " + user.getFullName());
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(FamilyDashboardActivity.this, "Error loading elderly data: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupMedicationRecyclerView() {
        medicationList = new ArrayList<>();
        medicationAdapter = new MedicationAdapter(this, medicationList);
        recyclerViewMedications.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMedications.setAdapter(medicationAdapter);
    }

    private void loadMedications() {
        String elderlyId = preferenceManager.getLinkedUserId();
        if (elderlyId != null) {
            databaseHelper.getMedicationsForElderly(elderlyId, new FirebaseDatabaseHelper.DataCallback<List<Medication>>() {
                @Override
                public void onSuccess(List<Medication> medications) {
                    medicationList.clear();
                    medicationList.addAll(medications);
                    medicationAdapter.notifyDataSetChanged();

                    // Show/hide no medications text
                    if (medicationList.isEmpty()) {
                        textViewNoMedications.setVisibility(View.VISIBLE);
                    } else {
                        textViewNoMedications.setVisibility(View.GONE);
                    }

                    // Update medication status
                    updateMedicationStatusSummary();
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(FamilyDashboardActivity.this, "Error loading medications: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadHealthData() {
        String elderlyId = preferenceManager.getLinkedUserId();
        if (elderlyId != null) {
            databaseHelper.getHealthDataForElderly(elderlyId, new FirebaseDatabaseHelper.DataCallback<List<HealthData>>() {
                @Override
                public void onSuccess(List<HealthData> healthDataList) {
                    if (!healthDataList.isEmpty()) {
                        // Sort by date (newest first)
                        Collections.sort(healthDataList, new Comparator<HealthData>() {
                            @Override
                            public int compare(HealthData o1, HealthData o2) {
                                return o2.getRecordedDate().compareTo(o1.getRecordedDate());
                            }
                        });

                        // Get the latest health data
                        HealthData latestData = healthDataList.get(0);

                        // Update UI
                        textViewTemperature.setText(latestData.getTemperature() + "°F");
                        textViewBloodPressure.setText(latestData.getBloodPressure());
                        textViewSugarLevel.setText(latestData.getSugarLevel() + " mg/dL");

                        // Hide the "no health data" message
                        textViewNoHealthDataYet.setVisibility(View.GONE);
                    } else {
                        textViewNoHealthDataYet.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(FamilyDashboardActivity.this, "Error loading health data: " + errorMessage, Toast.LENGTH_SHORT).show();
                    textViewNoHealthDataYet.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void loadDailyStatus() {
        String elderlyId = preferenceManager.getLinkedUserId();
        if (elderlyId != null) {
            databaseHelper.getLatestDailyCheckIn(elderlyId, new FirebaseDatabaseHelper.DataCallback<Map<String, Object>>() {
                @Override
                public void onSuccess(Map<String, Object> checkIn) {
                    String feeling = (String) checkIn.get("feeling");
                    long timestamp = (long) checkIn.get("timestamp");

                    // Update feeling status
                    textViewFeeling.setText(feeling);

                    // Set color based on feeling
                    if ("Good".equals(feeling)) {
                        textViewFeeling.setTextColor(getResources().getColor(R.color.green));
                    } else if ("Okay".equals(feeling)) {
                        textViewFeeling.setTextColor(getResources().getColor(R.color.yellow));
                    } else {
                        textViewFeeling.setTextColor(getResources().getColor(R.color.red));
                    }

                    // Calculate time since last check-in
                    long currentTime = System.currentTimeMillis();
                    long timeDiff = currentTime - timestamp;

                    // Convert to hours
                    long hours = timeDiff / (60 * 60 * 1000);

                    if (hours < 1) {
                        textViewLastCheckIn.setText("Just now");
                    } else if (hours == 1) {
                        textViewLastCheckIn.setText("1 hour ago");
                    } else {
                        textViewLastCheckIn.setText(hours + " hours ago");
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    textViewFeeling.setText("No data");
                    textViewLastCheckIn.setText("No data");
                }
            });
        }
    }

    private void updateMedicationStatusSummary() {
        int total = medicationList.size();
        int taken = 0;

        for (Medication medication : medicationList) {
            if (medication.isTaken()) {
                taken++;
            }
        }

        textViewMedicationStatus.setText(taken + "/" + total + " Taken");

        // Set color based on completion
        if (taken == total) {
            textViewMedicationStatus.setTextColor(getResources().getColor(R.color.green));
        } else if (taken > 0) {
            textViewMedicationStatus.setTextColor(getResources().getColor(R.color.yellow));
        } else {
            textViewMedicationStatus.setTextColor(getResources().getColor(R.color.red));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        authHelper.logoutUser();
        preferenceManager.clearSession();
        startActivity(new Intent(FamilyDashboardActivity.this, LoginActivity.class));
        finish();
    }
}