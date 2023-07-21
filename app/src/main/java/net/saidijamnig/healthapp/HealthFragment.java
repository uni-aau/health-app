package net.saidijamnig.healthapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.fragment.app.Fragment;

public class HealthFragment extends Fragment implements SensorEventListener {
    private TextView stepsTextView, pulseTextView, waterTextView, foodTextView;

    private int stepsCount = 0;
    private int waterCount = 0;
    private int foodCalories = 0;

    private SharedPreferences sharedPreferences;
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private Sensor heartRateSensor;

    public HealthFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_health, container, false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

        // Verknüpfung der Views mit den XML-Elementen
        stepsTextView = view.findViewById(R.id.stepsTextView);
        pulseTextView = view.findViewById(R.id.pulseTextView);
        waterTextView = view.findViewById(R.id.waterTextView);
        foodTextView = view.findViewById(R.id.foodTextView);

        Button pulseButton = view.findViewById(R.id.pulseButton);
        Button waterPlusButton = view.findViewById(R.id.waterPlusButton);
        Button waterMinusButton = view.findViewById(R.id.waterMinusButton);
        Button foodInputButton = view.findViewById(R.id.foodInputButton);

        // Klick-Listener für die Buttons
        pulseButton.setOnClickListener(v -> measurePulse());
        waterPlusButton.setOnClickListener(v -> incrementWaterCount());
        waterMinusButton.setOnClickListener(v -> decrementWaterCount());
        foodInputButton.setOnClickListener(v -> openFoodInput());

        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            return null;
        }

        if (heartRateSensor != null) {
            sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            return null;
        }

        loadSavedData();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Unregistriere den Schritt-Erkennungssensor, um Ressourcen freizugeben
        sensorManager.unregisterListener(this, stepSensor);
        sensorManager.unregisterListener(this, heartRateSensor);
    }

    @Override
    public void onPause() {
        super.onPause();
        saveData();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            countSteps();
        }
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            float[] values = event.values;
            if (values.length > 0) {
                int pulseRate = (int) values[0];
                setPulseRate(pulseRate);
            }
        }
    }

    private void loadSavedData() {
        stepsCount = sharedPreferences.getInt("stepsCount", 0);
        waterCount = sharedPreferences.getInt("waterCount", 0);
        foodCalories = sharedPreferences.getInt("foodCalories", 0);
        updateUI();
    }

    private void saveData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("stepsCount", stepsCount);
        editor.putInt("waterCount", waterCount);
        editor.putInt("foodCalories", foodCalories);
        editor.apply();
    }

    @SuppressLint("SetTextI18n")
    private void updateUI() {
        stepsTextView.setText("Steps: " + stepsCount);
        waterTextView.setText("Water: " + waterCount + " glasses");
        foodTextView.setText("Food: " + foodCalories + " kcal");
    }

    private void countSteps() {
        stepsCount++;
        updateUI();
    }

    private void measurePulse() {
        Sensor heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        if (heartRateSensor != null) {
            sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Sensor not available");
            builder.setMessage("Heart rate sensor is not available on this device.");
            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
            builder.show();
        }
    }


    @SuppressLint("SetTextI18n")
    private void setPulseRate(int pulseRate) {
        pulseTextView.setText("Pulse: " + pulseRate + " bpm");
    }

    private void incrementWaterCount() {
        waterCount++;
        updateUI();
    }

    private void decrementWaterCount() {
        if (waterCount > 0) {
            waterCount--;
            updateUI();
        }
    }

    private void openFoodInput() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter Calories");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String calories = input.getText().toString();
            if (!calories.isEmpty()) {
                foodCalories = Integer.parseInt(calories);
                updateUI();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
