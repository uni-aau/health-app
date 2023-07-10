package net.saidijamnig.healthapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import net.saidijamnig.healthapp.databinding.FragmentGpsBinding;

import java.util.Locale;

public class GpsFragment extends Fragment implements OnMapReadyCallback {
    private double totalDistance = 0.0;
    private GoogleMap mMap;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private int totalCalories = 0;
    private int seconds = 0;
    private int minutes = 0;
    private int hours = 0;
    private boolean isTracking = false;
    private SupportMapFragment mapFragment;
    private TextView durationTV, distanceTV, caloriesTV;
    private Button startTrackingButton, stopTrackingButton;
    private FragmentGpsBinding binding;
    private LocationManager locationManager;
    private boolean foundLocation = false;
    private CountDownTimer timer;
    private int elapsedTime = 0;
    private Handler handler;
    private Location previousLocation;
    private LocationListener locationListener;
    private FusedLocationProviderClient fusedLocationClient;


    public GpsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGpsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        handler = new Handler(Looper.getMainLooper());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        durationTV = binding.gpsTextviewDurationStatus;
        distanceTV = binding.gpsTextviewDistance;
        caloriesTV = binding.gpsTextviewCalories;
        startTrackingButton = binding.buttonGpsStart;
        startTrackingButton.setOnClickListener(view1 -> startTracking());
        stopTrackingButton = binding.buttonGpsStop;
        stopTrackingButton.setEnabled(false);
        stopTrackingButton.setOnClickListener(view1 -> stopTracking());

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        initializeStartValues();

        // Inflate the layout for this fragment
        return view;
    }

    private void startTracking() {
        if(!foundLocation) {
            Toast.makeText(getActivity(), "Error, no location was found!", Toast.LENGTH_SHORT).show();
            return;
        }
        initializeStartValues();

        if(!isTracking) {
            Log.d("TAG", "Starting tracking location");
            isTracking = true;
            stopTrackingButton.setEnabled(true);
            startTrackingButton.setEnabled(false);
            startTimer();
            trackLocation();
        }
    }

    private void startTimer() {
        Log.d("TAG", "Starting timer!");
        timer = new CountDownTimer(Integer.MAX_VALUE, 1000) {
            @Override
            public void onTick(long l) {
                elapsedTime += 1000;

                hours = (elapsedTime / (1000 * 60 * 60)) % 24;
                minutes = (elapsedTime / (1000 * 60)) % 60;
                seconds = (elapsedTime / 1000) % 60;

                setDurationValue();
            }

            @Override
            public void onFinish() {
                // Not used since it will be disabled by stopTrackingButton
            }
        }.start();
    }

    @SuppressLint("MissingPermission")
    private void trackLocation() {
        if(getRequiredPermissions()) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if(location != null) {
                            if(previousLocation == null) {
                                previousLocation = location;
                            } else {
                                double distance = previousLocation.distanceTo(location);
                                totalDistance += distance;
                                previousLocation = location;

                                    String formattedDistance = String.format(getString(R.string.text_gps_distance), String.valueOf(totalDistance));
                                    distanceTV.setText(formattedDistance);
                            }
                        }
                    })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to get current location", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(requireContext(), "Some permissions are missing!", Toast.LENGTH_SHORT).show();
        }

        long postInterval = 10000L;
        handler.postDelayed(this::trackLocation,postInterval);
    }

    private void stopTracking() {
        if(isTracking) {
            Log.d("TAG", "Stopping tracking location");
            isTracking = false;
            stopTrackingButton.setEnabled(false);
            startTrackingButton.setEnabled(true);
            stopTimer();
            handler.removeCallbacksAndMessages(null); // Resets all callbacks (e.g. tracking)
            previousLocation = null;
            totalDistance = 0.0;
            totalCalories = 0;
        }
    }

    private void stopTimer() {
        elapsedTime = 0;
        hours = 0;
        minutes = 0;
        seconds = 0;

        if(timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @SuppressLint("MissingPermission") // TODO
    // Todo check mit toast, ob gps location existiert
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (getRequiredPermissions()) {
            mMap = googleMap;
            mMap.setMyLocationEnabled(true);

            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                double latitude = lastKnownLocation.getLatitude();
                double longitude = lastKnownLocation.getLongitude();

                Log.d("TAG", "Latitude = " + latitude + " longitude = " + longitude);

                LatLng currentLocation = new LatLng(latitude, longitude);

                mMap.addMarker(new MarkerOptions().position(currentLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
                foundLocation = true;
            } else {
                foundLocation = false;
            }
        } else {
            Log.e("TAG", "Error resolving permissions for onMapReady - Not granted");
            Toast.makeText(requireContext(), "You need to grant permission to access location!", Toast.LENGTH_SHORT).show();
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

    // Todo extract everything regarding permission to permission handler and rework it
    private boolean getRequiredPermissions() {
        // Check location permissions
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void initializeStartValues() {
        setDurationValue();

        String formattedCalories = String.format(getString(R.string.text_gps_calories), String.valueOf(totalCalories));
        caloriesTV.setText(formattedCalories);
        String formattedDistance = String.format(getString(R.string.text_gps_distance), String.valueOf(totalDistance));
        distanceTV.setText(formattedDistance);
    }

    private void setDurationValue() {
        Log.d("TAG", "Setting duration value - seconds = " + seconds);
        String durationStatus = getString(R.string.text_gps_duration_status);
        String formattedDurationStatus = String.format(durationStatus, formatTime(hours), formatTime(minutes), formatTime(seconds));
        durationTV.setText(formattedDurationStatus);
    }

    private String formatTime(int value) {
        return String.format(Locale.getDefault(), "%02d", value); // two digits and the leading is a zero if necessary
    }

    /*
     * Tracking muss noch mit zB bewegung abgestimmt werden
     */
}