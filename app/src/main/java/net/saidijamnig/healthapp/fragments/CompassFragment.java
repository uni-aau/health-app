package net.saidijamnig.healthapp.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;

import net.saidijamnig.healthapp.R;
import net.saidijamnig.healthapp.databinding.FragmentCompassBinding;
import net.saidijamnig.healthapp.util.Compass;
import net.saidijamnig.healthapp.util.SOTWFormatter;

import java.util.Locale;

public class CompassFragment extends Fragment implements SensorEventListener, LocationListener {
    private static final String TAG = "CompassActivity";
    private ImageView compassImage;

    private Compass compass;
    private ImageView arrowView;
    private TextView sotwLabel;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private Sensor lightSensor;
    private SensorEventListener lightListener;
    private LocationManager locationManager;

    private float[] gravity;
    private float[] geomagnetic;
    private float azimuth;
    private float currentAzimuth;
    private SOTWFormatter sotwFormatter;

    private TextView orientationTextView;
    private TextView gpsOrientationTextView;
    private TextView altitudeTextView;
    private TextView latitudeTextView;
    private TextView longitudeTextView;
    private TextView brightnessTextView;

    private static final int REQUEST_LOCATION_PERMISSION = 1;


    public CompassFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentCompassBinding binding;
        binding = FragmentCompassBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        sotwFormatter = new SOTWFormatter(requireContext());
        compassImage = binding.imageWheel;
        arrowView = binding.imageWheel;
        sotwLabel = binding.sotwLabel;
        setupCompass();

        orientationTextView = binding.orientationTextView;
        gpsOrientationTextView = binding.gpsOrientationTextView;
        altitudeTextView = binding.altitudeTextView;
        latitudeTextView = binding.latitudeTextView;
        longitudeTextView = binding.longitudeTextView;
        brightnessTextView = binding.brightnessTextView;

        initializeGuiWithErrorMessage(getString(R.string.waiting_for_data));

        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);

        startLocationUpdates();
        checkBrightnessSensor();

        return view;
    }

    private boolean checkForLocationPermission() {
        // Check location permission
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);

            return false;
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "start compass");
        compass.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterEvents();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterEvents();
    }

    private void unregisterEvents() {
        compass.stop();
        locationManager.removeUpdates(this);

        // Unregisters the lightListener when the fragment is stopped
        if (lightSensor != null && lightListener != null) {
            sensorManager.unregisterListener(lightListener);
            lightListener = null;
        }

        sensorManager.unregisterListener(this, accelerometer);
        sensorManager.unregisterListener(this, magnetometer);
    }

    @Override
    public void onResume() {
        super.onResume();

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "stop compass");
        unregisterEvents();
    }

    private void setupCompass() {
        compass = new Compass(requireContext());
        Compass.CompassListener cl = getCompassListener();
        compass.setListener(cl);
    }

    private void initializeGuiWithErrorMessage(String errorMessage) {

        orientationTextView.setText(getString(R.string.orientation, errorMessage));
        gpsOrientationTextView.setText(getString(R.string.gps_orientation, errorMessage));
        altitudeTextView.setText(getString(R.string.altitude, errorMessage));
        latitudeTextView.setText(getString(R.string.latitude, errorMessage));
        longitudeTextView.setText(getString(R.string.longitude, errorMessage));
        brightnessTextView.setText(getString(R.string.brightness, errorMessage));
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
            float[] rotationMatrix = new float[9];
            boolean success = SensorManager.getRotationMatrix(rotationMatrix,
                    null, gravity, geomagnetic);
            if (success) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(rotationMatrix, orientation);
                azimuth = (float) Math.toDegrees(orientation[0]);
                azimuth = (azimuth + 360) % 360;

                Animation anim = new RotateAnimation(-currentAzimuth, -azimuth,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                currentAzimuth = azimuth;

                anim.setDuration(500);
                anim.setRepeatCount(0);
                anim.setFillAfter(true);

                compassImage.startAnimation(anim);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Not needed
    }


    public void updateLocationTextViews(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        double altitude = location.getAltitude();

        String orientationSuffix = getString(R.string.orientation_suffix);
        orientationTextView.setText(getString(R.string.orientation_with_suffix, String.format(Locale.getDefault(), "%.2f", azimuth), orientationSuffix));
        gpsOrientationTextView.setText(getString(R.string.gps_orientation, getDirection(azimuth)));
        String altitudeSuffix = getString(R.string.altitude_suffix);
        altitudeTextView.setText(getString(R.string.altitude_with_suffix, String.format(Locale.getDefault(), "%.2f", altitude), altitudeSuffix));
        latitudeTextView.setText(getString(R.string.latitude, String.format(Locale.getDefault(), "%.6f", latitude)));
        longitudeTextView.setText(getString(R.string.longitude, String.format(Locale.getDefault(),"%.6f", longitude)));
    }

    private void adjustArrow(float azimuth) {
        Animation an = new RotateAnimation(-currentAzimuth, -azimuth,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        currentAzimuth = azimuth;

        an.setDuration(500);
        an.setRepeatCount(0);
        an.setFillAfter(true);

        arrowView.startAnimation(an);
    }

    private void adjustSotwLabel(float azimuth) {
        sotwLabel.setText(sotwFormatter.format(azimuth));
    }

    private Compass.CompassListener getCompassListener() {
        return azimuth -> requireActivity().runOnUiThread(() -> {
            adjustArrow(azimuth);
            adjustSotwLabel(azimuth);
        });
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        if(checkForLocationPermission()) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.d("TAG", "Starting requesting location!");
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        0,
                        0,
                        this
                );
            } else {
                Log.e("TAG", "Error requesting location - Not enabled");
                Toast.makeText(requireContext(), getString(R.string.error_no_location_enabled), Toast.LENGTH_SHORT).show();
            }
        } else {
            initializeGuiWithErrorMessage(getString(R.string.compass_location_not_granted));
        }
    }

    private void checkBrightnessSensor() {
        if (lightSensor != null) {
            lightListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    float brightness = event.values[0];
                    brightnessTextView.setText(requireContext().getString(R.string.brightness, String.valueOf(brightness)));
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                }
            };

            sensorManager.registerListener(lightListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            brightnessTextView.setText(getString(R.string.lightsensor_no_sensor_error));
        }
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

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.w("TAG", "Location was changed!");
        updateLocationTextViews(location);
    }
}