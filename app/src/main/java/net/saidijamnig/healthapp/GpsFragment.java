package net.saidijamnig.healthapp;

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
import androidx.room.Room;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import net.saidijamnig.healthapp.database.AppDatabase;
import net.saidijamnig.healthapp.database.History;
import net.saidijamnig.healthapp.database.HistoryDao;
import net.saidijamnig.healthapp.databinding.FragmentGpsBinding;
import net.saidijamnig.healthapp.handler.PermissionHandler;
import net.saidijamnig.healthapp.services.LocationTrackingService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GpsFragment extends Fragment implements OnMapReadyCallback {
    public static final String ACTION_LOCATION_UPDATE = "ACTION_LOCATION_UPDATE";
    public static final String ACTION_DURATION_UPDATE = "ACTION_DURATION_UDPATE";
    FragmentGpsBinding binding;

    private static final String TAG = "GPS-Main";
    private static final String DB_TAG = "GPS-DB";
    private static List<LatLng> points = new ArrayList<>();
    private double totalDistance = 0.0;
    private GoogleMap mMap;
    private int totalCalories = 0;
    private int seconds = 0;
    private int minutes = 0;
    private int hours = 0;
    private boolean isTracking = false;
    private SupportMapFragment mapFragment;
    private TextView durationTV;
    private TextView distanceTV;
    private TextView caloriesTV;
    private Button startTrackingButton;
    private Button stopTrackingButton;
    private boolean foundLocation = false;
    private int elapsedDurationTimeInMilliSeconds = 0;
    private AppDatabase db;
    private HistoryDao historyDao;
    private String imageTrackAbsolutePath;
    private String imageName;
    private Spinner spinner;
    private String selectedActivityType;
    private Date currentDate;
    private BroadcastReceiver locationUpdateReceiver = new BroadcastReceiver() {
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
                    Log.d(TAG, String.valueOf(elapsedDurationTimeInMilliSeconds));
                    setDurationValue();
                }
            }
        }
    };

    public GpsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGpsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(locationUpdateReceiver, new IntentFilter(ACTION_DURATION_UPDATE));
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(locationUpdateReceiver, new IntentFilter(ACTION_LOCATION_UPDATE));

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
        caloriesTV = binding.gpsTextviewDistance; // TODO
        startTrackingButton = binding.buttonGpsStart;
        spinner = binding.gpsSpinnerType;
        stopTrackingButton = binding.buttonGpsStop;

        startTrackingButton.setOnClickListener(view1 -> startTracking());
        stopTrackingButton.setOnClickListener(view1 -> stopTracking());

        binding.gpsLogo.setImageResource(R.drawable.logo);
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
            startTrackingButton.setEnabled(false);
            stopTrackingButton.setEnabled(true);
            isTracking = true;

            initializeCurrentTrackingValues();
        } else {
            stopTrackingButton.setEnabled(false);
            startTrackingButton.setEnabled(true);
            initializeStartValues();
            isTracking = false;
        }
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
        if (db != null) db.close();
        db = Room.databaseBuilder(requireContext(), AppDatabase.class, "history")
                .fallbackToDestructiveMigration() // Deletes whole database when version gets changed
                .build();
        historyDao = db.historyDao();
    }

    /**
     * Starts new track with some checks if a location exists and whether a track already runs
     * Resets old tracking views and clears map
     */
    private void startTracking() {
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGpsEnabled || !isNetworkEnabled) {
            Toast.makeText(getActivity(), "Error, please enable your GPS and/or Internet Connection and try again!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!foundLocation) {
            Toast.makeText(getActivity(), "Error, no location was found!", Toast.LENGTH_SHORT).show();
            return;
        }

        initializeStartValues();
        mMap.clear();

        if (!isTracking) {
            Log.i(TAG, "Starting tracking location");
            Intent locationService = new Intent(requireContext(), LocationTrackingService.class);
            requireActivity().startService(locationService);

            isTracking = true;
            stopTrackingButton.setEnabled(true);
            startTrackingButton.setEnabled(false);
        }
    }

    /**
     * Formats the distance to only two digits after comma
     *
     * @return formatted String
     */
    private String formatDistance() {
        return String.format(Locale.getDefault(), Config.DISTANCE_FORMAT, totalDistance);
    }

    private void stopTracking() {
        if (isTracking) {
            Log.i(TAG, "Stopping tracking location");
            Intent locationService = new Intent(requireContext(), LocationTrackingService.class);
            requireActivity().stopService(locationService);

            generateCurrentDate();
            saveMapScreenshot();
            saveTrackToDatabase();
            resetTrackingVariables();

            stopTrackingButton.setEnabled(false);
            startTrackingButton.setEnabled(true);
        }
    }

    private void saveMapScreenshot() {
        Log.i(TAG, "Trying to make a screenshot");
        if (!points.isEmpty()) {
            calculateZoomLevel();
        }

        GoogleMap.SnapshotReadyCallback callback = snapshot -> {
            FileOutputStream outputStream = null;
            try {
                imageName = formatImageTrackName();
                File directory = requireActivity().getApplicationContext().getFilesDir();
                File file = new File(directory, imageName);
                imageTrackAbsolutePath = file.getAbsolutePath();
                outputStream = new FileOutputStream(file);
                snapshot.compress(Config.COMPRESS_FORMAT, Config.COMPRESS_QUALITY, outputStream);

                Log.i(TAG, "Successfully saved file to internal storage with path " + imageTrackAbsolutePath + " and name " + imageName);
            } catch (IOException e) {
                Log.e(TAG, "Error saving image: " + e);
            } finally {
                if (outputStream != null) {
                    try {
                        Log.i(TAG, "Outputstream successfully closed!");
                        outputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error closing Fileoutputstream: " + e);
                    }
                }
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
        return String.format(unformattedTrackName, formatCurrentDate(true));
    }

    private void calculateZoomLevel() {
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
            });
        }
    }

    private void resetTrackingVariables() {
        elapsedDurationTimeInMilliSeconds = 0;
        hours = 0;
        minutes = 0;
        seconds = 0;
        totalDistance = 0.0;
        totalCalories = 0;
        points.clear();
        imageTrackAbsolutePath = null;
        imageName = null;

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
        newHistoryEntry.activityDate = formatCurrentDate(false);
        newHistoryEntry.imageTrackName = formatImageTrackName();
        newHistoryEntry.fullImageTrackPath = imageTrackAbsolutePath;

        Thread thread = new Thread(() -> historyDao.insertNewHistoryEntry(newHistoryEntry));
        thread.start();
    }

    private void printDebugTracksToConsole() {
        Thread thread = new Thread(() -> {
            List<History> histories = historyDao.getWholeHistoryEntries();
            Log.i(DB_TAG, "All saved history tracks:");
            for (History history : histories) {
                String formattedString = String.format(Locale.getDefault(), "[ID %d] Distance = %s / Duration = %s / Activity = %s / Date = %s / ImageName = %s / ImagePath = %s",
                        history.uid, history.activityDistance, history.durationInMilliSeconds, history.activityType, history.activityDate, history.imageTrackName, history.fullImageTrackPath);
                Log.i(DB_TAG, formattedString);
            }
        });
        thread.start();
    }

    private void generateCurrentDate() {
        currentDate = new Date();
    }

    /**
     * Formats the currentDate with a specific format
     *
     * @param isImageTrackName - Determines if it should be generated for an image track name
     * @return formatted date
     */
    private String formatCurrentDate(boolean isImageTrackName) {
        String formattedDate;

        if (!isImageTrackName) formattedDate = Config.TIME_FORMAT_GENERAL;
        else formattedDate = Config.TIME_FORMAT_TRACK_NAME;

        SimpleDateFormat dateFormat = new SimpleDateFormat(formattedDate, Locale.getDefault());
        return dateFormat.format(currentDate);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (checkRequiredPermissions()) {
            mMap.setMyLocationEnabled(true);
            fetchLocationAndUpdateMap();
        } else {
            Log.e("TAG", "Error resolving permissions for onMapReady - Not granted");
            Toast.makeText(requireContext(), "You need to grant permission to access location!", Toast.LENGTH_SHORT).show();
            PermissionHandler.requestGpsPermissions(requireActivity());
        }
    }

    @SuppressLint("MissingPermission")
    private void fetchLocationAndUpdateMap() {
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
        return PermissionHandler.checkForRequiredPermissions(requireContext());
    }

    // TODO does not work
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionHandler.REQUEST_LOCATION_PERMISSION) {
            if (PermissionHandler.checkForRequiredPermissions(requireContext())) {
                Log.d(TAG, "Fetching new location (permission granted)");
                fetchLocationAndUpdateMap();
            } else {
                // Handle the case when the permissions are not granted.
                Toast.makeText(requireContext(), "You need to grant permission to access location!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Sets the start values for the GPS TextViews
     * Formats duration (to hours, minutes & seconds)
     */
    private void initializeStartValues() {
        setDurationValue();
        setGpsTrackTextViews();
    }

    private void setGpsTrackTextViews() {
        String formattedCalories = String.format(getString(R.string.text_gps_calories), String.valueOf(totalCalories));
        caloriesTV.setText(formattedCalories);
        String formattedDistance = String.format(getString(R.string.text_gps_distance), formatDistance());
        distanceTV.setText(formattedDistance);
    }

    private void setDurationValue() {
        hours = (elapsedDurationTimeInMilliSeconds / (1000 * 60 * 60)) % 24;
        minutes = (elapsedDurationTimeInMilliSeconds / (1000 * 60)) % 60;
        seconds = (elapsedDurationTimeInMilliSeconds / 1000) % 60;

        String durationStatus = getString(R.string.text_gps_duration_status);
        String formattedDurationStatus = String.format(durationStatus, formatTime(hours), formatTime(minutes), formatTime(seconds));
        durationTV.setText(formattedDurationStatus);
    }

    private String formatTime(int value) {
        return String.format(Locale.getDefault(), Config.DURATION_FORMAT, value); // two digits and the leading is a zero if necessary
    }
}