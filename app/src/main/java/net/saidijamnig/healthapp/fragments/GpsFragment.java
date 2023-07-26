package net.saidijamnig.healthapp.fragments;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import net.saidijamnig.healthapp.Config;
import net.saidijamnig.healthapp.R;
import net.saidijamnig.healthapp.database.AppDatabase;
import net.saidijamnig.healthapp.database.DatabaseHandler;
import net.saidijamnig.healthapp.database.History;
import net.saidijamnig.healthapp.database.HistoryDao;
import net.saidijamnig.healthapp.databinding.FragmentGpsBinding;
import net.saidijamnig.healthapp.services.LocationTrackingService;
import net.saidijamnig.healthapp.util.OnZoomCalculatedListener;
import net.saidijamnig.healthapp.util.PermissionHandler;
import net.saidijamnig.healthapp.util.TextFormatHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A fragment that allows users to track their GPS location, display the route on Google Maps,
 * and save the tracked activity to the Room database.
 */

public class GpsFragment extends Fragment implements OnMapReadyCallback {
    public static final String ACTION_LOCATION_UPDATE = "ACTION_LOCATION_UPDATE";
    public static final String ACTION_DURATION_UPDATE = "ACTION_DURATION_UDPATE";
    private static final String TAG = "GPS-Main";
    private static final String DB_TAG = "GPS-DB";
    private static List<LatLng> points = new ArrayList<>();
    FragmentGpsBinding binding;
    private double totalDistance = 0.0;
    private GoogleMap mMap;
    private boolean isTracking = false;
    private SupportMapFragment mapFragment;
    private TextView durationTV;
    private TextView distanceTV;
    private Button startTrackingButton;
    private Button stopTrackingButton;
    private boolean foundLocation = false;
    private int elapsedDurationTimeInMilliSeconds = 0;

    // BroadcastReceiver to receive location and duration updates from the tracking service
    private final BroadcastReceiver locationUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                if (intent.getAction().equals(ACTION_LOCATION_UPDATE)) { // Handles location updates
                    totalDistance = intent.getDoubleExtra("distance", 0.0);
                    LatLng latLng = intent.getParcelableExtra("latlng");

                    points.add(latLng);
                    handleLocationUpdates(false);
                } else if (intent.getAction().equals(ACTION_DURATION_UPDATE)) { // Handles duration updates
                    elapsedDurationTimeInMilliSeconds = intent.getIntExtra("time", 0);
                    setFormattedDurationValue();
                }
            }
        }
    };
    private HistoryDao historyDao;
    private String imageTrackAbsolutePath;
    private Spinner spinner;
    private String selectedActivityType;
    private Date currentDate;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGpsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(locationUpdateReceiver, new IntentFilter(ACTION_DURATION_UPDATE));
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(locationUpdateReceiver, new IntentFilter(ACTION_LOCATION_UPDATE));

        checkForegroundPermission();
        initializeDatabase();
        initializeGuiElements();
        initializeSpinner();

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initializeTrackingProcess();

        // Inflate the layout for this fragment
        return view;
    }

    private void initializeGuiElements() {
        durationTV = binding.gpsTextviewDurationStatus;
        distanceTV = binding.gpsTextviewDistance;
        startTrackingButton = binding.buttonGpsStart;
        spinner = binding.gpsSpinnerType;
        stopTrackingButton = binding.buttonGpsStop;

        startTrackingButton.setOnClickListener(view1 -> startTracking());
        stopTrackingButton.setOnClickListener(view1 -> stopTracking());

        binding.gpsLogo.setImageResource(R.drawable.logo);
    }

    private void checkForegroundPermission() {
        if (!PermissionHandler.checkForForegroundPermission(requireContext())) {
            PermissionHandler.requestForegroundPermission(requireActivity());
        }
    }

    private void initializeSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.spinner_values,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                selectedActivityType = adapterView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Get first item when no item was selected
                selectedActivityType = adapterView.getItemAtPosition(1).toString();
            }
        });

    }

    private void initializeTrackingProcess() {
        if (LocationTrackingService.isActive) {
            enableStopButtonAndDisableStartButton();
            isTracking = true;

            initializeCurrentTrackingValues();
        } else {
            disableStopButtonAndEnableStartButton();
            initializeStartValues();
            isTracking = false;
        }
    }

    private void enableStopButtonAndDisableStartButton() {
        startTrackingButton.setEnabled(false);
        startTrackingButton.setBackgroundResource(R.drawable.general_button_layout_disabled);
        stopTrackingButton.setEnabled(true);
        stopTrackingButton.setBackgroundResource(R.drawable.general_button_layout);
    }

    private void disableStopButtonAndEnableStartButton() {
        startTrackingButton.setEnabled(true);
        startTrackingButton.setBackgroundResource(R.drawable.general_button_layout);
        stopTrackingButton.setEnabled(false);
        stopTrackingButton.setBackgroundResource(R.drawable.general_button_layout_disabled);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(locationUpdateReceiver);
    }

    private void initializeCurrentTrackingValues() {
        totalDistance = LocationTrackingService.totalDistance;
        elapsedDurationTimeInMilliSeconds = LocationTrackingService.elapsedDurationTimeInMilliSeconds;

        initializeStartValues();
        handleLocationUpdates(true);
    }

    /**
     * Receives a location and processes it
     * Sets track textViews and updates Google Map
     *
     * @param isInitializationProcess is needed to determine the proper zoom of the GoogleMap
     */
    private void handleLocationUpdates(boolean isInitializationProcess) {
        setGpsTrackTextViews();

        LatLng latLng = points.get(points.size() - 1);
        Log.d(TAG, "LatLng = " + latLng + " distance = " + totalDistance);

        mapFragment.getMapAsync(map -> {
            map.clear();
            map.addPolyline(new PolylineOptions()
                    .width(Config.MAP_LINE_WIDTH)
                    .color(Config.MAP_LINE_COLOR)
                    .addAll(points)
            );
            float currentSelectedZoom = isInitializationProcess ? Config.GENERAL_CAMERA_ZOOM : map.getCameraPosition().zoom;
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, currentSelectedZoom));
        });
    }

    /**
     * Initializes Room Database with History Table
     */
    private void initializeDatabase() {
        AppDatabase db = DatabaseHandler.getInitializeDatabase(requireContext());
        historyDao = db.historyDao();
    }

    /**
     * Starts new track with some checks if a location exists and whether a track already runs
     * Resets old tracking views and clears map
     */
    private void startTracking() {
        if (!checkForTrackingRequirements()) return;
        initializeStartValues();
        mMap.clear();

        if (!isTracking) {
            Log.i(TAG, "Starting tracking location");
            Intent locationService = new Intent(requireContext(), LocationTrackingService.class);
            requireActivity().startForegroundService(locationService); // used to utilize gps in background

            isTracking = true;
            enableStopButtonAndDisableStartButton();
        }
    }

    private boolean checkForTrackingRequirements() {
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGpsEnabled || !isNetworkEnabled) {
            Toast.makeText(getActivity(), "Error, please enable your GPS and/or Internet Connection and try again!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!foundLocation) {
            Toast.makeText(getActivity(), "Error, no location was found!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void stopTracking() {
        if (isTracking) {
            Log.i(TAG, "Stopping tracking location");
            Intent locationService = new Intent(requireContext(), LocationTrackingService.class);
            requireActivity().stopService(locationService);

            generateCurrentDate();
            processMapScreenshot();
            saveTrackToDatabase();
            resetTrackingVariables();

            disableStopButtonAndEnableStartButton();
        }
    }

    private void generateCurrentDate() {
        currentDate = new Date();
    }

    private void processMapScreenshot() {
        Log.i(TAG, "Trying to make a screenshot");
        if (!points.isEmpty()) {
            calculateZoomLevel(this::saveMapScreenshot);
        } else {
            saveMapScreenshot();
        }
    }

    private void saveMapScreenshot() {
        GoogleMap.SnapshotReadyCallback callback = snapshot -> {
            String imageName = formatImageTrackName();
            File directory = requireActivity().getApplicationContext().getFilesDir();
            File file = new File(directory, imageName);
            String imageTrackAbsolutePath = file.getAbsolutePath();

            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                snapshot.compress(Config.COMPRESS_FORMAT, Config.COMPRESS_QUALITY, outputStream);
                Log.i(TAG, "Successfully saved file to internal storage with path " + imageTrackAbsolutePath + " and name " + imageName);
            } catch (IOException e) {
                Log.e(TAG, "Error saving image: " + e);
            }
        };

        mMap.snapshot(callback);
    }

    /**
     * Generates image name of the saved track
     *
     * @return formatted image name
     */
    private String formatImageTrackName() {
        String unformattedTrackName = Config.TRACK_NAME_FORMAT;
        String formattedDate = TextFormatHandler.formatCurrentDate(true, currentDate);
        return String.format(unformattedTrackName, formattedDate);
    }

    private void calculateZoomLevel(OnZoomCalculatedListener listener) {
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng point : points) {
            boundsBuilder.include(point);
        }

        LatLngBounds bounds = boundsBuilder.build();
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                int width = getResources().getDisplayMetrics().widthPixels;
                int height = getResources().getDisplayMetrics().heightPixels;
                int padding = (int) (height * 0.05f);

                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding));
                listener.onZoomCalculated();
            });
        }
    }

    private void resetTrackingVariables() {
        elapsedDurationTimeInMilliSeconds = 0;
        totalDistance = 0.0;
        points.clear();
        imageTrackAbsolutePath = null;

        isTracking = false;
    }

    /**
     * Saves a new track to the room database
     */
    private void saveTrackToDatabase() {
        Log.i(DB_TAG, "Saving track to database!");
        History newHistoryEntry = new History();

        newHistoryEntry.activityType = String.valueOf(selectedActivityType);
        newHistoryEntry.durationInMilliSeconds = String.valueOf(elapsedDurationTimeInMilliSeconds);
        newHistoryEntry.activityDistance = formatDistance();
        newHistoryEntry.activityDate = TextFormatHandler.formatCurrentDate(false, currentDate);
        newHistoryEntry.imageTrackName = formatImageTrackName();
        newHistoryEntry.fullImageTrackPath = imageTrackAbsolutePath;

        Thread thread = new Thread(() -> historyDao.insertNewHistoryEntry(newHistoryEntry));
        thread.start();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (checkRequiredPermissions()) {
            fetchLocationAndUpdateMap();
        } else {
            Log.e("TAG", "Error resolving permissions for onMapReady - Not granted");
            Toast.makeText(requireContext(), "You need to grant permission to access location!", Toast.LENGTH_SHORT).show();
            PermissionHandler.requestGpsPermission(requireActivity());
        }
    }

    @SuppressLint("MissingPermission")
    private void fetchLocationAndUpdateMap() {
        mMap.setMyLocationEnabled(true);
        LocationManager locationManager;
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation != null) {
            updateMapWithLocation(lastKnownLocation);
        } else {
            // Requests new location when location was found
            foundLocation = false;
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, location -> {
                Log.i(TAG, "Found a new location! " + location);
                updateMapWithLocation(location);
            }, null);
        }
    }

    private void updateMapWithLocation(Location lastKnownLocation) {
        foundLocation = true;
        double latitude = lastKnownLocation.getLatitude();
        double longitude = lastKnownLocation.getLongitude();

        Log.d(TAG, "Latitude = " + latitude + " longitude = " + longitude);
        LatLng currentLocation = new LatLng(latitude, longitude);

        mMap.addMarker(new MarkerOptions().position(currentLocation).icon(BitmapDescriptorFactory.defaultMarker(Config.DEFAULT_MARKER_COLOR)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, Config.GENERAL_CAMERA_ZOOM));
    }

    private boolean checkRequiredPermissions() {
        return PermissionHandler.checkForRequiredGpsPermission(requireContext());
    }

    // TODO does not work
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        System.out.println("Requestcode" + requestCode);
        if (requestCode == PermissionHandler.REQUEST_LOCATION_PERMISSION) {
            if (PermissionHandler.checkForRequiredGpsPermission(requireContext())) {
                Log.d(TAG, "Fetching new location (permission granted)");
                fetchLocationAndUpdateMap();
            } else {
                // Handle the case when the permissions are not granted.
                Toast.makeText(requireContext(), "You need to grant permission to access location!2", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Sets the start values for the GPS TextViews
     * Formats duration (to hours, minutes & seconds)
     */
    private void initializeStartValues() {
        setFormattedDurationValue();
        setGpsTrackTextViews();
    }

    private void setGpsTrackTextViews() {
        String formattedDistance = String.format(getString(R.string.text_gps_distance), formatDistance());
        distanceTV.setText(formattedDistance);
    }

    /**
     * Formats the distance to only two digits after comma
     *
     * @return formatted String
     */
    private String formatDistance() {
        return String.format(Locale.getDefault(), Config.DISTANCE_FORMAT, totalDistance);
    }

    private void setFormattedDurationValue() {
        String unformattedDurationStatus = getString(R.string.text_gps_duration_status);
        String formattedDurationStatus = TextFormatHandler.getFormattedDurationTime(elapsedDurationTimeInMilliSeconds, unformattedDurationStatus);
        durationTV.setText(formattedDurationStatus);
    }
}