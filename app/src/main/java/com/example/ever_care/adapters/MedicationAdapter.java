package com.example.ever_care.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ever_care.R;
import com.example.ever_care.models.Medication;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder> {

    private Context context;
    private List<Medication> medicationList;
    private OnMedicationTakenListener onMedicationTakenListener;

    public MedicationAdapter(Context context, List<Medication> medicationList) {
        this.context = context;
        this.medicationList = medicationList;
    }

    public interface OnMedicationTakenListener {
        void onMedicationTaken(Medication medication, boolean taken);
    }

    public void setOnMedicationTakenListener(OnMedicationTakenListener listener) {
        this.onMedicationTakenListener = listener;
    }

    @NonNull
    @Override
    public MedicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medication, parent, false);
        return new MedicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicationViewHolder holder, int position) {
        Medication medication = medicationList.get(position);

        holder.textViewMedicationName.setText(medication.getName());
        holder.textViewDosage.setText("Dosage: " + medication.getDosage());
        holder.textViewTime.setText("Time: " + medication.getReminderTime());

        // Update button state based on whether medication has been taken
        if (medication.isTaken()) {
            holder.buttonTaken.setText("Taken ✓");
            holder.buttonTaken.setBackgroundTintList(context.getColorStateList(R.color.green));
        } else {
            holder.buttonTaken.setText(R.string.taken);
            holder.buttonTaken.setBackgroundTintList(context.getColorStateList(R.color.primary));
        }

        holder.buttonTaken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onMedicationTakenListener != null) {
                    onMedicationTakenListener.onMedicationTaken(medication, true);
                }
            }
        });

        holder.buttonNotTaken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onMedicationTakenListener != null) {
                    onMedicationTakenListener.onMedicationTaken(medication, false);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return medicationList.size();
    }

    public static class MedicationViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMedicationName, textViewDosage, textViewTime;
        Button buttonTaken, buttonNotTaken;

        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMedicationName = itemView.findViewById(R.id.textViewMedicationName);
            textViewDosage = itemView.findViewById(R.id.textViewDosage);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            buttonTaken = itemView.findViewById(R.id.buttonTaken);
            buttonNotTaken = itemView.findViewById(R.id.buttonNotTaken);
        }
    }
}