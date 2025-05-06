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
import com.example.ever_care.adapters.HealthDataAdapter;
import com.example.ever_care.adapters.MedicationAdapter;
import com.example.ever_care.models.HealthData;
import com.example.ever_care.models.Medication;
import com.example.ever_care.utils.FirebaseAuthHelper;
import com.example.ever_care.utils.FirebaseDatabaseHelper;
import com.example.ever_care.utils.PreferenceManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ElderlyDashboardActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView textViewWelcome, textViewNoMedications, textViewNoHealthData;
    private Button buttonGood, buttonOkay, buttonNotWell, buttonAddMedication, buttonAddHealthData;
    private RecyclerView recyclerViewMedications, recyclerViewHealthData;

    private PreferenceManager preferenceManager;
    private FirebaseAuthHelper authHelper;
    private FirebaseDatabaseHelper databaseHelper;
    private MedicationAdapter medicationAdapter;
    private HealthDataAdapter healthDataAdapter;

    private List<Medication> medicationList;
    private List<HealthData> healthDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elderly_dashboard);

        preferenceManager = new PreferenceManager(getApplicationContext());
        authHelper = new FirebaseAuthHelper();
        databaseHelper = new FirebaseDatabaseHelper();

        // Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize views
        textViewWelcome = findViewById(R.id.textViewWelcome);
        textViewNoMedications = findViewById(R.id.textViewNoMedications);
        textViewNoHealthData = findViewById(R.id.textViewNoHealthData);

        buttonGood = findViewById(R.id.buttonGood);
        buttonOkay = findViewById(R.id.buttonOkay);
        buttonNotWell = findViewById(R.id.buttonNotWell);
        buttonAddMedication = findViewById(R.id.buttonAddMedication);
        buttonAddHealthData = findViewById(R.id.buttonAddHealthData);

        recyclerViewMedications = findViewById(R.id.recyclerViewMedications);
        recyclerViewHealthData = findViewById(R.id.recyclerViewHealthData);

        // Set welcome message
        textViewWelcome.setText("Welcome, " + preferenceManager.getUserName() + "!");

        // Set up recycler views
        setupMedicationRecyclerView();
        setupHealthDataRecyclerView();

        // Set click listeners
        buttonGood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDailyCheckIn("Good");
            }
        });

        buttonOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDailyCheckIn("Okay");
            }
        });

        buttonNotWell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDailyCheckIn("Not Well");
            }
        });

        buttonAddMedication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ElderlyDashboardActivity.this, MedicationDetailsActivity.class));
            }
        });

        buttonAddHealthData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ElderlyDashboardActivity.this, HealthDataActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this screen
        loadMedications();
        loadHealthData();
    }

    private void setupMedicationRecyclerView() {
        medicationList = new ArrayList<>();
        medicationAdapter = new MedicationAdapter(this, medicationList);
        recyclerViewMedications.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMedications.setAdapter(medicationAdapter);

        // Set medication taken callback
        medicationAdapter.setOnMedicationTakenListener(new MedicationAdapter.OnMedicationTakenListener() {
            @Override
            public void onMedicationTaken(Medication medication, boolean taken) {
                updateMedicationStatus(medication, taken);
            }
        });
    }

    private void setupHealthDataRecyclerView() {
        healthDataList = new ArrayList<>();
        healthDataAdapter = new HealthDataAdapter(this, healthDataList);
        recyclerViewHealthData.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHealthData.setAdapter(healthDataAdapter);
    }

    private void loadMedications() {
        databaseHelper.getMedicationsForElderly(preferenceManager.getUserId(), new FirebaseDatabaseHelper.DataCallback<List<Medication>>() {
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
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(ElderlyDashboardActivity.this, "Error loading medications: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadHealthData() {
        databaseHelper.getHealthDataForElderly(preferenceManager.getUserId(), new FirebaseDatabaseHelper.DataCallback<List<HealthData>>() {
            @Override
            public void onSuccess(List<HealthData> healthData) {
                healthDataList.clear();
                healthDataList.addAll(healthData);
                healthDataAdapter.notifyDataSetChanged();

                // Show/hide no health data text
                if (healthDataList.isEmpty()) {
                    textViewNoHealthData.setVisibility(View.VISIBLE);
                } else {
                    textViewNoHealthData.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(ElderlyDashboardActivity.this, "Error loading health data: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendDailyCheckIn(String feeling) {
        databaseHelper.saveDailyCheckIn(preferenceManager.getUserId(), feeling, new FirebaseDatabaseHelper.DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Toast.makeText(ElderlyDashboardActivity.this, "Daily check-in: " + feeling, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(ElderlyDashboardActivity.this, "Error saving check-in: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMedicationStatus(Medication medication, boolean taken) {
        Date takenTime = taken ? new Date() : null;
        databaseHelper.updateMedicationStatus(medication.getId(), taken, takenTime, new FirebaseDatabaseHelper.DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Toast.makeText(ElderlyDashboardActivity.this,
                        medication.getName() + " marked as " + (taken ? "taken" : "not taken"),
                        Toast.LENGTH_SHORT).show();

                // Refresh medications list
                loadMedications();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(ElderlyDashboardActivity.this, "Error updating medication: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
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
        startActivity(new Intent(ElderlyDashboardActivity.this, LoginActivity.class));
        finish();
    }
}