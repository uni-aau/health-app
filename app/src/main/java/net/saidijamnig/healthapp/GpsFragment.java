package net.saidijamnig.healthapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import net.saidijamnig.healthapp.databinding.FragmentGpsBinding;

import java.util.Locale;

public class GpsFragment extends Fragment implements OnMapReadyCallback {
    private double totalDistance = 0.0;
    private int totalCalories = 0;
    private int seconds = 0;
    private int minutes = 0;
    private int hours = 0;
    private boolean isTracking = false;
    private SupportMapFragment mapFragment;
    private TextView durationTV, distanceTV, caloriesTV;
    private Button startTrackingButton, stopTrackingButton;
    private FragmentGpsBinding binding;


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

    @Override
    public void onMapReady(GoogleMap googleMap) {

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