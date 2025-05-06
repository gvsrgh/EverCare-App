package com.example.ever_care.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ever_care.R;
import com.example.ever_care.models.HealthData;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HealthDataAdapter extends RecyclerView.Adapter<HealthDataAdapter.HealthDataViewHolder> {

    private Context context;
    private List<HealthData> healthDataList;
    private SimpleDateFormat dateFormat;

    public HealthDataAdapter(Context context, List<HealthData> healthDataList) {
        this.context = context;
        this.healthDataList = healthDataList;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public HealthDataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_health_data, parent, false);
        return new HealthDataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HealthDataViewHolder holder, int position) {
        HealthData healthData = healthDataList.get(position);

        holder.textViewDate.setText("Date: " + dateFormat.format(healthData.getRecordedDate()));
        holder.textViewTemperature.setText(healthData.getTemperature() + "°F");
        holder.textViewBloodPressure.setText(healthData.getBloodPressure());
        holder.textViewSugarLevel.setText(healthData.getSugarLevel() + " mg/dL");
    }

    @Override
    public int getItemCount() {
        return healthDataList.size();
    }

    public static class HealthDataViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDate, textViewTemperature, textViewBloodPressure, textViewSugarLevel;

        public HealthDataViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewTemperature = itemView.findViewById(R.id.textViewTemperature);
            textViewBloodPressure = itemView.findViewById(R.id.textViewBloodPressure);
            textViewSugarLevel = itemView.findViewById(R.id.textViewSugarLevel);
        }
    }
}