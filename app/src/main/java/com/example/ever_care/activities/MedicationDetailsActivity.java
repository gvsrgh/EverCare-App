package com.example.ever_care.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.ever_care.R;
import com.example.ever_care.models.Medication;
import com.example.ever_care.services.MedicationAlarmReceiver;
import com.example.ever_care.utils.FirebaseDatabaseHelper;
import com.example.ever_care.utils.PreferenceManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MedicationDetailsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText editTextMedicationName, editTextDosage, editTextFrequency;
    private EditText editTextStartDate, editTextEndDate, editTextReminderTime;
    private Button buttonSaveMedication;
    private ProgressBar progressBar;

    private PreferenceManager preferenceManager;
    private FirebaseDatabaseHelper databaseHelper;
    private Calendar startDateCalendar, endDateCalendar, reminderTimeCalendar;
    private SimpleDateFormat dateFormat, timeFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_details);

        preferenceManager = new PreferenceManager(getApplicationContext());
        databaseHelper = new FirebaseDatabaseHelper();

        // Initialize date and time formats
        dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        // Initialize calendars
        startDateCalendar = Calendar.getInstance();
        endDateCalendar = Calendar.getInstance();
        endDateCalendar.add(Calendar.MONTH, 1); // Default end date is 1 month from now
        reminderTimeCalendar = Calendar.getInstance();
        reminderTimeCalendar.set(Calendar.HOUR_OF_DAY, 8);
        reminderTimeCalendar.set(Calendar.MINUTE, 0);

        // Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize views
        editTextMedicationName = findViewById(R.id.editTextMedicationName);
        editTextDosage = findViewById(R.id.editTextDosage);
        editTextFrequency = findViewById(R.id.editTextFrequency);
        editTextStartDate = findViewById(R.id.editTextStartDate);
        editTextEndDate = findViewById(R.id.editTextEndDate);
        editTextReminderTime = findViewById(R.id.editTextReminderTime);
        buttonSaveMedication = findViewById(R.id.buttonSaveMedication);
        progressBar = findViewById(R.id.progressBar);

        // Set default values
        editTextStartDate.setText(dateFormat.format(startDateCalendar.getTime()));
        editTextEndDate.setText(dateFormat.format(endDateCalendar.getTime()));
        editTextReminderTime.setText(timeFormat.format(reminderTimeCalendar.getTime()));

        // Set click listeners
        editTextStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(startDateCalendar, editTextStartDate);
            }
        });

        editTextEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(endDateCalendar, editTextEndDate);
            }
        });

        editTextReminderTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });

        buttonSaveMedication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    saveMedication();
                }
            }
        });
    }

    private void showDatePickerDialog(final Calendar calendar, final EditText editText) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        editText.setText(dateFormat.format(calendar.getTime()));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        reminderTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        reminderTimeCalendar.set(Calendar.MINUTE, minute);
                        editTextReminderTime.setText(timeFormat.format(reminderTimeCalendar.getTime()));
                    }
                },
                reminderTimeCalendar.get(Calendar.HOUR_OF_DAY),
                reminderTimeCalendar.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    private boolean validateInputs() {
        if (TextUtils.isEmpty(editTextMedicationName.getText().toString().trim())) {
            editTextMedicationName.setError("Medication name is required");
            return false;
        }

        if (TextUtils.isEmpty(editTextDosage.getText().toString().trim())) {
            editTextDosage.setError("Dosage is required");
            return false;
        }

        if (TextUtils.isEmpty(editTextFrequency.getText().toString().trim())) {
            editTextFrequency.setError("Frequency is required");
            return false;
        }

        return true;
    }


    private void saveMedication() {
        progressBar.setVisibility(View.VISIBLE);
        buttonSaveMedication.setEnabled(false);

        String name = editTextMedicationName.getText().toString().trim();
        String dosage = editTextDosage.getText().toString().trim();
        String frequency = editTextFrequency.getText().toString().trim();
        Date startDate = startDateCalendar.getTime();
        Date endDate = endDateCalendar.getTime();
        String reminderTime = timeFormat.format(reminderTimeCalendar.getTime());

        // Determine if user is elderly or family member
        String elderlyId;
        String createdBy = preferenceManager.getUserId();

        if (preferenceManager.isElderly()) {
            elderlyId = preferenceManager.getUserId();
        } else {
            elderlyId = preferenceManager.getLinkedUserId();
        }

        Log.d("MedicationDetails", "Creating medication: " + name + " for elderly ID: " + elderlyId);

        // Create medication object
        Medication medication = new Medication(
                null, // ID will be generated by Firebase
                name,
                dosage,
                frequency,
                startDate,
                endDate,
                reminderTime,
                elderlyId,
                createdBy
        );

        // Save to Firebase
        databaseHelper.saveMedication(medication, new FirebaseDatabaseHelper.DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Log.d("MedicationDetails", "Medication saved successfully");
                progressBar.setVisibility(View.GONE);
                buttonSaveMedication.setEnabled(true);
                Toast.makeText(MedicationDetailsActivity.this, "Medication saved", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("MedicationDetails", "Error saving medication: " + errorMessage);
                progressBar.setVisibility(View.GONE);
                buttonSaveMedication.setEnabled(true);
                Toast.makeText(MedicationDetailsActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    // In the saveMedication method, after saving to Firebase
//databaseHelper.saveMedication(medication, new FirebaseDatabaseHelper.DataCallback<Boolean>() {
//        @Override
//        public void onSuccess(Boolean result) {
//            // Schedule medication alarm
//            MedicationAlarmReceiver.scheduleMedicationAlarm(MedicationDetailsActivity.this, medication);
//
//            progressBar.setVisibility(View.GONE);
//            buttonSaveMedication.setEnabled(true);
//            Toast.makeText(MedicationDetailsActivity.this, "Medication saved", Toast.LENGTH_SHORT).show();
//            finish();
//        }
//
//        @Override
//        public void onError(String errorMessage) {
//            progressBar.setVisibility(View.GONE);
//            buttonSaveMedication.setEnabled(true);
//            Toast.makeText(MedicationDetailsActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
//        }
//    });
}