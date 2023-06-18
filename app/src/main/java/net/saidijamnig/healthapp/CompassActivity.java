package net.saidijamnig.healthapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class CompassActivity extends AppCompatActivity {
    private ImageView compassImage;
    private TextView orientationTextView, gpsOrientationTextView, altitudeTextView,
            latitudeTextView, longitudeTextView, brightnessTextView;

    private SensorManager sensorManager;
    private Sensor accelerometer, magnetometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        compassImage = findViewById(R.id.compassImage);
        orientationTextView = findViewById(R.id.orientationTextView);
        gpsOrientationTextView = findViewById(R.id.gpsOrientationTextView);
        altitudeTextView = findViewById(R.id.altitudeTextView);
        latitudeTextView = findViewById(R.id.latitudeTextView);
        longitudeTextView = findViewById(R.id.longitudeTextView);
        brightnessTextView = findViewById(R.id.brightnessTextView);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }
}
