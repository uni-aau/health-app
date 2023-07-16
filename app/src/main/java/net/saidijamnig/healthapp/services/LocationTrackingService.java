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
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;

import net.saidijamnig.healthapp.Config;
import net.saidijamnig.healthapp.GpsFragment;

import java.util.ArrayList;
import java.util.List;

public class LocationTrackingService extends Service {
    private static final String TAG = "GPS-Main";
    public static boolean isActive = false;

    private static final String CHANNEL_ID = "gps_tracking_channel";
    private static final int NOTIFICATION_ID = 1;


    private double totalDistance = 0.0;
    private int totalCalories = 0;
    private CountDownTimer timer;
    private int elapsedDurationTimeInMilliSeconds = 0;
    private Handler handler;
    private Location previousLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private List<LatLng> points = new ArrayList<>();
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

    /**
     * Starts the tracking timer
     */
    private void startTimer() {
        Log.i(TAG, "Starting timer!");
        timer = new CountDownTimer(Integer.MAX_VALUE, 1000) {
            @Override
            public void onTick(long l) {
                elapsedDurationTimeInMilliSeconds += 1000;
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
        stopTimer();
        handler.removeCallbacksAndMessages(null); // Resets all callbacks (e.g. tracking)
        resetTrackingVariables();
        isActive = false;
    }

    /**
     * Cancels the existing countdown timer
     */
    private void stopTimer() {
        elapsedDurationTimeInMilliSeconds = 0;

        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void resetTrackingVariables() {
        previousLocation = null;
        totalDistance = 0.0;
        totalCalories = 0;
        points.clear();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTracking();
    }
}
