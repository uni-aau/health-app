package net.saidijamnig.healthapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class StepCounterWorker extends Worker implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private SharedPreferences sharedPreferences;
    private int stepsCount = 0;

    public StepCounterWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        initializeStepCount();
        return Result.success();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            countSteps();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed
    }

    private void initializeStepCount() {
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void countSteps() {
        stepsCount++;
        saveData();
    }

    private void saveData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("stepsCount", stepsCount);
        editor.apply();
    }
}
