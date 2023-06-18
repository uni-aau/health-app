package net.saidijamnig.healthapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


public class CompassActivity extends AppCompatActivity implements SensorEventListener, LocationListener{
    private ImageView compassImage;
    private TextView orientationTextView, gpsOrientationTextView, altitudeTextView,
            latitudeTextView, longitudeTextView, brightnessTextView;

    private SensorManager sensorManager;
    private Sensor accelerometer, magnetometer;
    private float[] gravity, geomagnetic;
    private float azimuth = 0f;
    private float currentAzimuth = 0f;

    private LocationManager locationManager;

    private static final int REQUEST_LOCATION_PERMISSION = 1;

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
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }


    @Override
        protected void onResume() {
            super.onResume();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSION);
                return;
            }
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    0, 0, this);
        }

        @Override
        protected void onPause() {
            super.onPause();
            sensorManager.unregisterListener(this);
            locationManager.removeUpdates(this);
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                gravity = event.values;
            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                geomagnetic = event.values;
            }
            if (gravity != null && geomagnetic != null) {
            }
        }

        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    private String getDirection(float azimuth) {
        if (azimuth >= 315 || azimuth < 45) {
            return "North";
        } else if (azimuth >= 45 && azimuth < 135) {
            return "East";
        } else if (azimuth >= 135 && azimuth < 225) {
            return "South";
        } else {
            return "West";
        }
    }

}
