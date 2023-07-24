package net.saidijamnig.healthapp.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import net.saidijamnig.healthapp.Config;
import net.saidijamnig.healthapp.R;
import net.saidijamnig.healthapp.StepCounterWorker;
import net.saidijamnig.healthapp.database.AppDatabase;
import net.saidijamnig.healthapp.database.DatabaseHandler;
import net.saidijamnig.healthapp.database.Health;
import net.saidijamnig.healthapp.database.HealthDao;
import net.saidijamnig.healthapp.databinding.FragmentHealthBinding;
import net.saidijamnig.healthapp.util.PermissionHandler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Fragment that displays health-related data, including step count, pulse rate, water intake, and food calories.
 * This fragment uses sensors to measure step count and pulse rate, and allows the user to input food calories.
 */

public class HealthFragment extends Fragment implements SensorEventListener {
    private TextView stepsTextView;
    private TextView pulseTextView;
    private TextView waterTextView;
    private TextView foodTextView;
    private int stepsCount = 0;
    private int waterCount = 0;
    private int foodCalories = 0;
    private int pulseRate = 0;

    private HealthDao healthDao;
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private Sensor heartRateSensor;

    public HealthFragment() {
        // Requires public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentHealthBinding binding;
        binding = FragmentHealthBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        if (!PermissionHandler.checkForActivityRecognitionPermission(requireContext()))
            PermissionHandler.requestActivityRecognitionPermission(requireActivity());

        stepsTextView = binding.textViewSteps;
        pulseTextView = binding.textViewPulse;
        waterTextView = binding.textViewWater;
        foodTextView = binding.textViewCalories;

        Button pulseButton = binding.pulseButton;
        Button waterPlusButton = binding.waterPlusButton;
        Button waterMinusButton = binding.waterMinusButton;
        Button foodInputButton = binding.foodInputButton;

        pulseButton.setOnClickListener(v -> measurePulse());
        waterPlusButton.setOnClickListener(v -> incrementWaterCount());
        waterMinusButton.setOnClickListener(v -> decrementWaterCount());
        foodInputButton.setOnClickListener(v -> openFoodInput());

        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);

        initializeDatabase();
        loadSavedData();
        initializeStepCount();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        sensorManager.unregisterListener(this, stepSensor);
        sensorManager.unregisterListener(this, heartRateSensor);
    }

    /**
     * Called when the fragment is no longer in the resumed state.
     * Saves the current health-related data (steps count, water intake, and food calories) into SharedPreferences.
     */
    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, heartRateSensor);
        saveData();
    }

    private void initializeDatabase() {
        AppDatabase db = DatabaseHandler.getInitializeDatabase(requireContext());
        healthDao = db.healthDao();
    }

    /**
     * Called when there is a change in sensor values.
     * If the event is from the step detector sensor, increments the step count and updates the UI.
     * If the event is from the heart rate sensor, updates the pulse rate and updates the UI.
     *
     * @param event The SensorEvent containing the sensor data.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            countSteps();
        }
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            float[] values = event.values;
            if (values.length > 0) {
                pulseRate = (int) values[0];
                setPulseRate();
            }
        }
    }

    /**
     * Initializes the step count by registering the step detector sensor if available.
     * If the step detector sensor is not available, displays a message indicating that no sensor is available.
     * If the required activity recognition permission is not granted, requests the permission.
     */
    private void initializeStepCount() {
        if (PermissionHandler.checkForActivityRecognitionPermission(requireContext())) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            if (stepSensor != null) {
                sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                stepsTextView.setText(getString(R.string.steps_count_with_no_suffix, "No sensor available!"));
            }
        } else {
            stepsTextView.setText(getString(R.string.steps_count_with_no_suffix, getString(R.string.text_no_permission)));
            PermissionHandler.requestActivityRecognitionPermission(requireActivity());
        }

//        sensorManager.unregisterListener(this, stepSensor);

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                StepCounterWorker.class,
                10, //update interval in min
                TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
                "StepCounterWork",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
        );
    }


    /**
     * Loads the saved health-related data (steps count, water intake, and food calories) from Room Database.
     * Updates the UI with the loaded data.
     */
    private void loadSavedData() {
        Thread thread = new Thread(() -> {
            String currentDate = generateCurrentDate();
            Health healthEntry = healthDao.selectEntryByCurrentDate(currentDate);

            if (healthEntry != null) {
                stepsCount = healthEntry.lastStepsAmount;
                waterCount = healthEntry.waterAmount;
                foodCalories = healthEntry.foodAmount;
            } else {
                stepsCount = 0;
                waterCount = 0;
                foodCalories = 0;
                healthDao.deleteAll();
            }

            updateUI();
        });
        thread.start();
    }

    /**
     * Saves the current health-related data (steps count, water intake, and food calories) into Room Database.
     */
    private void saveData() {
        Health healthEntry = new Health();
        healthEntry.waterAmount = waterCount;
        healthEntry.foodAmount = foodCalories;
        healthEntry.lastStepsAmount = stepsCount;
        healthEntry.date = generateCurrentDate();

        Thread thread = new Thread(() -> healthDao.insertNewHealthEntry(healthEntry));
        thread.start();
    }

    private String generateCurrentDate() {
        Date date = new Date();

        SimpleDateFormat dateFormat = new SimpleDateFormat(Config.TIME_FORMAT_HEALTH, Locale.getDefault());
        return dateFormat.format(date);
    }

    /**
     * Updates the UI elements (TextViews) with the current health-related data (steps count, pulse rate, water intake, and food calories).
     */
    private void updateUI() {
        updateStepsCountText();
        setPulseRate();
        updateFoodCountText();
        updateWaterCountText();
    }

    /**
     * Updates the food calories TextView with the current food calories value.
     */
    private void updateFoodCountText() {
        foodTextView.setText(getString(R.string.text_food, String.valueOf(foodCalories)));
    }

    /**
     * Updates the water intake TextView with the current water intake value.
     * Uses a plural resource to display the correct string format based on the water count.
     */
    private void updateWaterCountText() {
        String formattedWater = getResources().getQuantityString(R.plurals.text_water_glasses, waterCount, waterCount);
        waterTextView.setText(formattedWater);
    }

    /**
     * Updates the steps count TextView with the current steps count value.
     * Uses a plural resource to display the correct string format based on the step count.
     */
    private void updateStepsCountText() {
        String formattedSteps = getResources().getQuantityString(R.plurals.text_steps, stepsCount, stepsCount);
        stepsTextView.setText(formattedSteps);
    }

    /**
     * Increments the step count and updates the steps count TextView.
     */
    private void countSteps() {
        stepsCount++;
        updateStepsCountText();
    }

    /**
     * Measures the pulse rate by registering the heart rate sensor if available.
     * If the heart rate sensor is not available, displays a dialog indicating that the sensor is not available on the device.
     * If the required body sensor permission is not granted, updates the pulse rate TextView with a permission message and requests the permission.
     */
    private void measurePulse() {
        if (PermissionHandler.checkForBodySensorPermission(requireContext())) {
            heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
            if (heartRateSensor != null) {
                pulseTextView.setText(getString(R.string.text_pulse_without_suffix, getString(R.string.text_wating_for_data)));
                sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);

            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Sensor not available");
                builder.setMessage("Heart rate sensor is not available on this device.");
                builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                builder.show();
            }
        } else {
            pulseTextView.setText(getString(R.string.text_pulse_without_suffix, getString(R.string.text_no_permission)));
            PermissionHandler.requestBodySensorPermission(requireActivity());
        }
    }

    /**
     * Updates the pulse rate TextView with the current pulse rate value.
     */
    private void setPulseRate() {
        pulseTextView.setText(getString(R.string.text_pulse, String.valueOf(pulseRate)));
    }

    /**
     * Increments the water count and updates the water intake TextView.
     */
    private void incrementWaterCount() {
        waterCount++;
        updateWaterCountText();
    }

    /**
     * Decrements the water count and updates the water intake TextView.
     * Ensures that the water count does not go below 0.
     */
    private void decrementWaterCount() {
        if (waterCount > 0) {
            waterCount--;
            updateWaterCountText();
        }
    }

    /**
     * Opens a dialog to input food calories.
     * Validates the input and updates the food calories TextView with the input value if valid.
     * Displays an error message if the input is not valid (empty or too high).
     */
    private void openFoodInput() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(getString(R.string.enter_calories_text));

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton(getString(R.string.ok_button_text), (dialog, which) -> {
            String caloriesInput = input.getText().toString();

            if (!caloriesInput.isEmpty()) {
                if (caloriesInput.length() > Config.MAX_CALORIES_LENGTH) {
                    Toast.makeText(requireContext(), getString(R.string.error_too_much_calories), Toast.LENGTH_SHORT).show();
                    dialog.cancel();
                    return;
                }

                foodCalories = Integer.parseInt(caloriesInput);
                updateFoodCountText();
            }
        });

        builder.setNegativeButton(getString(R.string.cancel_button_text), (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed
    }
}
