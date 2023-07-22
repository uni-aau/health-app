package net.saidijamnig.healthapp;

import android.Manifest;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class CompassFragment extends Fragment implements SensorEventListener, LocationListener {
    private static final String TAG = "CompassActivity";
    private ImageView compassImage;

    private Compass compass;
    private ImageView arrowView;
    private TextView sotwLabel;

    private SensorManager sensorManager;
    private Sensor accelerometer, magnetometer;
    private LocationManager locationManager;

    private float[] gravity, geomagnetic;
    private float azimuth;
    private float currentAzimuth;
    private SOTWFormatter sotwFormatter;

    private TextView orientationTextView, gpsOrientationTextView, altitudeTextView, latitudeTextView, longitudeTextView, brightnessTextView;

    private static final int REQUEST_LOCATION_PERMISSION = 1;


    public CompassFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_compass, container, false);

        sotwFormatter = new SOTWFormatter(requireContext());
        compassImage = view.findViewById(R.id.image_wheel);
        arrowView = view.findViewById(R.id.image_wheel);
        setupCompass();

        orientationTextView = view.findViewById(R.id.orientationTextView);
        gpsOrientationTextView = view.findViewById(R.id.gpsOrientationTextView);
        altitudeTextView = view.findViewById(R.id.altitudeTextView);
        latitudeTextView = view.findViewById(R.id.latitudeTextView);
        longitudeTextView = view.findViewById(R.id.longitudeTextView);
        brightnessTextView = view.findViewById(R.id.brightnessTextView);

        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);

        // Check location permissions
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            startLocationUpdates();
        }
        checkBrightnessSensor();

        return view;
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
        sensorManager.unregisterListener(this);
        locationManager.removeUpdates(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                0, 0, this);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "stop compass");
        compass.stop();
    }

    private void setupCompass() {
        compass = new Compass(requireContext());
        Compass.CompassListener cl = getCompassListener();
        compass.setListener(cl);
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
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        double altitude = location.getAltitude();

        orientationTextView.setText("Orientation: " + String.format("%.2f", azimuth) + "°");
        gpsOrientationTextView.setText("GPS Orientation: " + getDirection(azimuth));
        altitudeTextView.setText("Altitude: " + String.format("%.2f", altitude) + "m");
        latitudeTextView.setText("Latitude: " + String.format("%.6f", latitude));
        longitudeTextView.setText("Longitude: " + String.format("%.6f", longitude));

    }

    private void adjustArrow(float azimuth) {
        Log.d(TAG, "will set rotation from " + currentAzimuth + " to " + azimuth);

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
        return new Compass.CompassListener() {
            @Override
            public void onNewAzimuth(final float azimuth) {
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adjustArrow(azimuth);
                    }
                });
            }
        };
    }

    private void startLocationUpdates() {
        LocationManager locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    double altitude = location.getAltitude();

                    String gpsOrientation = String.format("Latitude: %.2f\nLongitude: %.2f", latitude, longitude);

                    gpsOrientationTextView.setText(gpsOrientation);
                    altitudeTextView.setText(String.valueOf(altitude));
                    latitudeTextView.setText(String.valueOf(latitude));
                    longitudeTextView.setText(String.valueOf(longitude));
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    private void checkBrightnessSensor() {
        SensorManager sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (lightSensor != null) {
            SensorEventListener lightListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    float brightness = event.values[0];
                    brightnessTextView.setText("Brightness: " + brightness);
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                }
            };

            sensorManager.registerListener(lightListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            brightnessTextView.setText("No light sensor available");
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
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}