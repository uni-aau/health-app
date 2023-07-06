package net.saidijamnig.healthapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LongDef;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdate;
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

        durationTV = binding.gpsTextviewDurationStatus;
        distanceTV = binding.gpsTextviewDistance;
        caloriesTV = binding.gpsTextviewCalories;
        startTrackingButton = binding.buttonGpsStart;
        startTrackingButton.setOnClickListener(view1 -> startTracking());
        stopTrackingButton = binding.buttonGpsStop;
        stopTrackingButton.setOnClickListener(view1 -> stopTracking());

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        initializeStartValues();

        // Inflate the layout for this fragment
        return view;
    }

    int test = 0;

    private void startTracking() {
        durationTV.setText(Integer.toString(test));
        test++;
        System.out.println("Start Tracking works fine!");
    }

    private void stopTracking() {
        System.out.println("Stop Tracking works fine!");
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
            if(lastKnownLocation != null) {
                double latitude = lastKnownLocation.getLatitude();
                double longitude = lastKnownLocation.getLongitude();

                Log.d("TAG", "Latitude = " + latitude + " longitude = " + longitude);

                LatLng currentLocation = new LatLng(latitude, longitude);

                mMap.addMarker(new MarkerOptions().position(currentLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
            }
        } else {
            Log.e("TAG", "Error resolving permissions for onMapReady - Not granted");
            Toast.makeText(requireContext(), "You need to grant permission to access location!", Toast.LENGTH_SHORT).show();
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);}


    }

    // Todo extract everything regarding permission to permission handler and rework it
    private boolean getRequiredPermissions() {
        // Check location permissions
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void initializeStartValues() {
        String durationStatus = getString(R.string.text_gps_duration_status);
        String formattedDurationStatus = String.format(durationStatus, formatTime(hours), formatTime(minutes), formatTime(seconds));
        durationTV.setText(formattedDurationStatus);

        String formattedCalories = String.format(getString(R.string.text_gps_calories), String.valueOf(totalCalories));
        caloriesTV.setText(formattedCalories);
        String formattedDistance = String.format(getString(R.string.text_gps_distance), String.valueOf(totalDistance));
        distanceTV.setText(formattedDistance);
    }

    private String formatTime(int value) {
        return String.format(Locale.getDefault(), "%02d", value); // two digits and the leading is a zero if necessary
    }
}