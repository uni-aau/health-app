package net.saidijamnig.healthapp.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.saidijamnig.healthapp.Config;
import net.saidijamnig.healthapp.R;
import net.saidijamnig.healthapp.databinding.FragmentCompassBinding;
import net.saidijamnig.healthapp.util.Compass;
import net.saidijamnig.healthapp.util.PermissionHandler;

import java.util.Locale;

public class CompassFragment extends Fragment implements SensorEventListener, LocationListener {
    private static final String TAG = "CompassFragment";
    private ImageView compassImage;
    private Compass compass;
    private ImageView arrowView;
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
    private TextView orientationTextView;
    private TextView gpsOrientationTextView;
    private TextView altitudeTextView;
    private TextView latitudeTextView;
    private TextView longitudeTextView;
    private TextView brightnessTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentCompassBinding binding = FragmentCompassBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        compassImage = binding.imageWheel;
        arrowView = binding.imageWheel;
        setupCompass();

        orientationTextView = binding.orientationTextView;
        gpsOrientationTextView = binding.gpsOrientationTextView;
        altitudeTextView = binding.altitudeTextView;
        latitudeTextView = binding.latitudeTextView;
        longitudeTextView = binding.longitudeTextView;
        brightnessTextView = binding.brightnessTextView;

        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        initializeLocationTextViewsWithErrorMessage(getString(R.string.waiting_for_data));
        if (checkCompassSensorAvailability())
            initializeCompassTextViewsWithErrorMessage(getString(R.string.waiting_for_data));
        else
            initializeCompassTextViewsWithErrorMessage(getString(R.string.error_no_compass_sensor_available));

        locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);

        startLocationUpdates();
        checkBrightnessSensor();

        return view;
    }

    private boolean checkForLocationPermission() {
        if (!PermissionHandler.checkForRequiredGpsPermission(requireContext())) {
            PermissionHandler.requestGpsPermission(requireActivity());
            return false;
        }
        return true;
    }

    private boolean checkCompassSensorAvailability() {
        return accelerometer != null && magnetometer != null;
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

        if (checkCompassSensorAvailability()) {
            sensorManager.unregisterListener(this, accelerometer);
            sensorManager.unregisterListener(this, magnetometer);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (checkCompassSensorAvailability()) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
        }
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

    private void initializeCompassTextViewsWithErrorMessage(String errorMessage) {
        orientationTextView.setText(getString(R.string.orientation, errorMessage));
        gpsOrientationTextView.setText(getString(R.string.gps_orientation, errorMessage));
    }

    private void initializeLocationTextViewsWithErrorMessage(String errorMessage) {
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
            boolean success = SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic);
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
                updateCompassTextViews();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Not needed
    }

    private void updateCompassTextViews() {
        String orientationSuffix = getString(R.string.orientation_suffix);
        orientationTextView.setText(getString(R.string.orientation_with_suffix, String.format(Locale.getDefault(), "%.2f", azimuth), orientationSuffix));
        gpsOrientationTextView.setText(getString(R.string.gps_orientation, getDirection(azimuth)));
    }


    public void updateLocationTextViews(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        double altitude = location.getAltitude();

        String altitudeSuffix = getString(R.string.altitude_suffix);
        altitudeTextView.setText(getString(R.string.altitude_with_suffix, String.format(Locale.getDefault(), "%.2f", altitude), altitudeSuffix));
        latitudeTextView.setText(getString(R.string.latitude, String.format(Locale.getDefault(), "%.6f", latitude)));
        longitudeTextView.setText(getString(R.string.longitude, String.format(Locale.getDefault(), "%.6f", longitude)));
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

    private Compass.CompassListener getCompassListener() {
        return azimuth -> requireActivity().runOnUiThread(() -> adjustArrow(azimuth));
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        if (checkForLocationPermission()) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.d(TAG, "Starting requesting location!");
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        Config.COMPASS_TRACKING_UPDATE_INTERVAL,
                        Config.COMPASS_TRACKING_MIN_UPDATE_DISTANCE,
                        this
                );
            } else {
                Log.e(TAG, "Error requesting location - Not enabled");
                initializeLocationTextViewsWithErrorMessage(getString(R.string.error_no_location_enabled));
            }
        } else {
            initializeLocationTextViewsWithErrorMessage(getString(R.string.text_no_permission));
        }
    }

    private void checkBrightnessSensor() {
        if (lightSensor != null) {
            lightListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    float brightness = event.values[0];
                    brightnessTextView.setText(getString(R.string.brightness, String.valueOf(brightness)));
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    // Not needed
                }
            };

            sensorManager.registerListener(lightListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            brightnessTextView.setText(getString(R.string.brightness, getString(R.string.lightsensor_no_sensor_error)));
        }
    }

    private String getDirection(float azimuth) {
        if (azimuth >= 315 || azimuth < 45) {
            return getString(R.string.direction_north);
        } else if (azimuth >= 45 && azimuth < 135) {
            return getString(R.string.direction_east);
        } else if (azimuth >= 135 && azimuth < 225) {
            return getString(R.string.direction_south);
        } else {
            return getString(R.string.direction_west);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.d(TAG, "Location was changed!");
        updateLocationTextViews(location);
    }
}
