package net.saidijamnig.healthapp.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import net.saidijamnig.healthapp.Config;
import net.saidijamnig.healthapp.GpsFragment;
import net.saidijamnig.healthapp.database.AppDatabase;
import net.saidijamnig.healthapp.database.HistoryDao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LocationTrackingService extends Service {
    private static final String TAG = "GPS-Main";
    private static final String DB_TAG = "GPS-DB";
    public static boolean isActive = false;

    private static final String CHANNEL_ID = "gps_tracking_channel";
    private static final int NOTIFICATION_ID = 1;


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

    private LocalBroadcastManager broadcastManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        handler = new Handler(Looper.getMainLooper());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        broadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        createNotificationChannel(); // TODO
        Log.d("Test", "Start command");
        startTracking();
        return super.onStartCommand(intent, flags, startId);
    }

    private void startTracking() {
        Log.i(TAG, "Starting tracking location");
        isActive = true;
        startTimer();
        trackLocation();
    }

    private void startTimer() {
        Log.i(TAG, "Starting timer!");
        timer = new CountDownTimer(Integer.MAX_VALUE, 1000) {
            @Override
            public void onTick(long l) {
                elapsedDurationTimeInMilliSeconds += 1000;
                Log.d("Test", String.valueOf(elapsedDurationTimeInMilliSeconds));
                sendDurationValueBroadcast();
            }

            @Override
            public void onFinish() {
                // Not used since it will be disabled by stopTrackingButton
            }
        }.start();
    }

    private void sendDurationValueBroadcast() {
        Intent intent = new Intent(GpsFragment.ACTION_DURATION_UPDATE);
        intent.putExtra("time", elapsedDurationTimeInMilliSeconds);
        broadcastManager.sendBroadcast(intent);
    }


    @SuppressLint("MissingPermission")
    private void trackLocation() {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        if (previousLocation == null) {
                            previousLocation = location;
                        } else {
                            double distance = previousLocation.distanceTo(location);
                            totalDistance += distance;
                            previousLocation = location;

                            // Visualization of distance path
                            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                            sendLocationBroadcast(currentLatLng);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to get current location", Toast.LENGTH_SHORT).show());
        handler.postDelayed(this::trackLocation, Config.MAP_UPDATE_INTERVAL);
    }

    private void sendLocationBroadcast(LatLng currentLatLng) {
        Intent intent = new Intent(GpsFragment.ACTION_LOCATION_UPDATE);
        intent.putExtra("latlng", currentLatLng);
        intent.putExtra("distance", totalDistance);
        broadcastManager.sendBroadcast(intent);
    }


    private void stopTracking() {
        Log.i(TAG, "Stopping tracking location");
//            generateCurrentDate();
//            saveMapScreenshot();
//            saveTrackToDatabase();
        isActive = false;
        stopTimer();
        handler.removeCallbacksAndMessages(null); // Resets all callbacks (e.g. tracking)
        resetTrackingVariables();
    }

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

    // TODO angleichen
    private void resetTrackingVariables() {
        previousLocation = null;
        totalDistance = 0.0;
        totalCalories = 0;
        points.clear();
        imageTrackAbsolutePath = null;
        imageName = null;

        isTracking = false;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTracking();

    }
}
