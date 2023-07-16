package net.saidijamnig.healthapp;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.room.Room;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;

import net.saidijamnig.healthapp.database.AppDatabase;
import net.saidijamnig.healthapp.database.History;
import net.saidijamnig.healthapp.database.HistoryDao;
import net.saidijamnig.healthapp.databinding.FragmentGpsBinding;
import net.saidijamnig.healthapp.handler.PermissionHandler;
import net.saidijamnig.healthapp.services.LocationTrackingService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GpsFragment extends Fragment implements OnMapReadyCallback {
    public static final String ACTION_LOCATION_UPDATE = "ACTION_LOCATION_UPDATE";
    public static final String ACTION_DURATION_UPDATE = "ACTION_DURATION_UDPATE";
    public static final String ACTION_TRACKING_STOPPED = "ACTION_TRACKING_STOPPED";

    private boolean isServiceBound = false;

    private static final String TAG = "GPS-Main";
    private static final String DB_TAG = "GPS-DB";

    private double totalDistance = 0.0;
    private GoogleMap mMap;
    private static final int PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 1;
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
    private CountDownTimer timer;
    private int elapsedDurationTimeInMilliSeconds = 0;
    private Handler handler;
    private Location previousLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private List<LatLng> points = new ArrayList<>();
    private AppDatabase db;
    private HistoryDao historyDao;
    private Button printTracksButton; // only for debug
    private String imageTrackAbsolutePath;
    private String imageName;
    private Date currentDate;

    public GpsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        MapsInitializer.initialize(requireContext(), MapsInitializer.Renderer.LATEST, renderer -> Log.d(TAG, "onMapsSdkInitialized!"));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentGpsBinding binding;
        binding = FragmentGpsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

//        handler = new Handler(Looper.getMainLooper());
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(locationUpdateReceiver, new IntentFilter(ACTION_DURATION_UPDATE));
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(locationUpdateReceiver, new IntentFilter(ACTION_LOCATION_UPDATE));

        initializeDatabase();

        durationTV = binding.gpsTextviewDurationStatus;
        distanceTV = binding.gpsTextviewDistance;
        caloriesTV = binding.gpsTextviewCalories;
        startTrackingButton = binding.buttonGpsStart;
        startTrackingButton.setOnClickListener(view1 -> startTracking());
        stopTrackingButton = binding.buttonGpsStop;
        stopTrackingButton.setOnClickListener(view1 -> stopTracking());
        printTracksButton = binding.buttonGpsDebug;
        printTracksButton.setOnClickListener(view1 -> printDebugTracksToConsole());

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if(!LocationTrackingService.isActive) {
            stopTrackingButton.setEnabled(false);
            startTrackingButton.setEnabled(true);
            initializeStartValues();
            isTracking = false;
        } else {
            startTrackingButton.setEnabled(false);
            stopTrackingButton.setEnabled(true);
            isTracking = true;
        }

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(locationUpdateReceiver);
    }

    private BroadcastReceiver locationUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                if (intent.getAction().equals(ACTION_LOCATION_UPDATE)) {
                    Log.d("DEBUG", "Works");
                    totalDistance = intent.getDoubleExtra("distance", 0.0);
                    LatLng latLng = intent.getParcelableExtra("latlng");
                    handleLocationUpdates(latLng);
                } else if (intent.getAction().equals(ACTION_DURATION_UPDATE)) {
                    // Handle tracking started event
                    elapsedDurationTimeInMilliSeconds = intent.getIntExtra("time", 0);
                    Log.d(TAG, String.valueOf(elapsedDurationTimeInMilliSeconds));
                    setDurationValue();
                } else if (intent.getAction().equals(ACTION_TRACKING_STOPPED)) {
//                    handleTrackingStopped();
                }
            }
        }
    };

    private void handleLocationUpdates(LatLng latLng) {
        Log.d("DEBUG", "LatLng = " + latLng + " distance = " + totalDistance);

        String formattedTotalDistance = formatDistance();
        String formattedDistance = String.format(getString(R.string.text_gps_distance), formattedTotalDistance);
        distanceTV.setText(formattedDistance);

        points.add(latLng);
        mapFragment.getMapAsync(map -> {
            map.clear();
            map.addPolyline(new PolylineOptions()
                    .width(Config.MAP_LINE_WIDTH)
                    .color(Config.MAP_LINE_COLOR)
                    .addAll(points)
            );
            float currentSelectedZoom = map.getCameraPosition().zoom;
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
     * Starts new track
     * Resets old tracking views and clears map
     */
    private void startTracking() {
        if (!foundLocation) {
            Toast.makeText(getActivity(), "Error, no location was found!", Toast.LENGTH_SHORT).show();
            return;
        }

        initializeStartValues();
        mMap.clear();

        if (!isTracking) {
            Intent locationService = new Intent(requireContext(), LocationTrackingService.class);
            requireActivity().startService(locationService);
//            requireActivity().bindService(locationService, serviceConnection)
            // TODO SERVICE

            Log.i(TAG, "Starting tracking location");
            isTracking = true;
            stopTrackingButton.setEnabled(true);
            startTrackingButton.setEnabled(false);
//            startTimer();
//            trackLocation();
        }
    }

    /**
     * Starts the tracking timer
     */
    private void startTimer() {
        Log.i(TAG, "Starting timer!");
        timer = new CountDownTimer(Integer.MAX_VALUE, 1000) {
            @Override
            public void onTick(long l) {
                elapsedDurationTimeInMilliSeconds += 1000;

                hours = (elapsedDurationTimeInMilliSeconds / (1000 * 60 * 60)) % 24;
                minutes = (elapsedDurationTimeInMilliSeconds / (1000 * 60)) % 60;
                seconds = (elapsedDurationTimeInMilliSeconds / 1000) % 60;

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
        if (checkRequiredPermissions()) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            if (previousLocation == null) {
                                previousLocation = location;
                            } else {
                                double distance = previousLocation.distanceTo(location);
                                totalDistance += distance;
                                previousLocation = location;

                                String formattedTotalDistance = formatDistance();
                                String formattedDistance = String.format(getString(R.string.text_gps_distance), formattedTotalDistance);
                                distanceTV.setText(formattedDistance);

                                // Visualization of distance path
                                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                String debugValues = "Accuracy = " + location.getAccuracy() + " Speed = " + location.getSpeed() + " Other stuff = " + location.getVerticalAccuracyMeters();
                                Log.d(TAG, debugValues);
                                Snackbar.make(requireView(), debugValues, Snackbar.LENGTH_SHORT).show();

                                points.add(currentLatLng);
                                mapFragment.getMapAsync(map -> {
                                    map.clear();
                                    map.addPolyline(new PolylineOptions()
                                            .width(Config.MAP_LINE_WIDTH)
                                            .color(Config.MAP_LINE_COLOR)
                                            .addAll(points)
                                    );
                                    float currentSelectedZoom = map.getCameraPosition().zoom;
                                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, currentSelectedZoom));
                                });
                            }
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to get current location", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(requireContext(), "Some permissions are missing!", Toast.LENGTH_SHORT).show();
            PermissionHandler.requestGpsPermissions(requireActivity());
        }

        handler.postDelayed(this::trackLocation, Config.MAP_UPDATE_INTERVAL);
    }

    /**
     * Formats the distance to only two digits after comma
     *
     * @return formatted String
     */
    private String formatDistance() {
        return String.format(Locale.getDefault(), "%.2f", totalDistance);
    }

    /*
    Möglichkeiten um Distanzproblem zu lösen:
     * Accelerometer checken
     * Accuracy < 15-10 nur nehmen und alles andere melden
     *  Activity STILL - https://medium.com/@saishaddai/how-to-recognize-when-the-user-is-not-moving-in-android-be6dba7a90bb
     */

    private void stopTracking() {
        if (isTracking) {
            Intent locationService = new Intent(requireContext(), LocationTrackingService.class);
            requireActivity().stopService(locationService);

            // TODO SERVICE
            Log.i(TAG, "Stopping tracking location");
//            generateCurrentDate();
//            saveMapScreenshot();
//            saveTrackToDatabase();
//            stopTimer();
//            handler.removeCallbacksAndMessages(null); // Resets all callbacks (e.g. tracking)
            resetTrackingVariables();

            stopTrackingButton.setEnabled(false);
            startTrackingButton.setEnabled(true);
        }
    }

    private void saveMapScreenshot() {
        Log.i(TAG, "Trying to make a screenshot");
        GoogleMap.SnapshotReadyCallback callback = snapshot -> {
            FileOutputStream outputStream = null;
            try {
                imageName = formatImageTrackName();
                File directory = requireActivity().getApplicationContext().getFilesDir();
                File file = new File(directory, imageName);
                imageTrackAbsolutePath = file.getAbsolutePath();
                outputStream = new FileOutputStream(file);
                snapshot.compress(Config.COMPRESS_FORMAT, Config.COMPRESS_QUALITY, outputStream);
                System.out.println(imageName + " 2");

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

    // TODO
    private float calculateZoomLevel(List<LatLng> line) {
        return 0.0F;
    }

    private void resetTrackingVariables() {
        previousLocation = null;
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

        newHistoryEntry.activityCalories = String.valueOf(totalCalories);
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
                String formattedString = String.format(Locale.getDefault(), "[ID %d] Distance = %s / Duration = %s / Calories = %s / Date = %s / ImageName = %s / ImagePath = %s",
                        history.uid, history.activityDistance, history.durationInMilliSeconds, history.activityCalories, history.activityDate, history.imageTrackName, history.fullImageTrackPath);
                Log.i(DB_TAG, formattedString);
            }
//            historyDao.deleteAll();
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

        SimpleDateFormat dateFormat = new SimpleDateFormat(formattedDate);
        return dateFormat.format(currentDate);
    }

    /**
     * Cancels the existing countdown timer
     */
    private void stopTimer() {
        elapsedDurationTimeInMilliSeconds = 0;
        hours = 0;
        minutes = 0;
        seconds = 0;

        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        if (checkRequiredPermissions()) {
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
//        foundLocation = true;
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
     * Sets the start values for the GPS textviews (e.g. duration)
     */
    private void initializeStartValues() {
        setDurationValue();

        String formattedCalories = String.format(getString(R.string.text_gps_calories), String.valueOf(totalCalories));
        caloriesTV.setText(formattedCalories);
        String formattedDistance = String.format(getString(R.string.text_gps_distance), String.valueOf(totalDistance));
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

    /*
     * Tracking muss noch mit zB bewegung abgestimmt werden
     * Fetchen der Location, wenn keine gefunden
     */
}