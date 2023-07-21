package net.saidijamnig.healthapp.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;

import net.saidijamnig.healthapp.Config;
import net.saidijamnig.healthapp.fragments.GpsFragment;
import net.saidijamnig.healthapp.MainActivity;
import net.saidijamnig.healthapp.R;
import net.saidijamnig.healthapp.util.PermissionHandler;

public class LocationTrackingService extends Service {
    private static final String TAG = "GPS-Main";
    private static final String CHANNEL_ID = "gps_tracking_channel";
    private static final int NOTIFICATION_ID = 1;
    public static boolean isActive = false;
    public static double totalDistance = 0.0;
    public static int elapsedDurationTimeInMilliSeconds = 0;
    private int totalCalories = 0;
    private CountDownTimer timer;
    private Handler handler;
    private Location previousLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private LocalBroadcastManager broadcastManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initializeGpsNotification();

        handler = new Handler(Looper.getMainLooper());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        broadcastManager = LocalBroadcastManager.getInstance(this);
    }

    private void initializeGpsNotification() {
        if (PermissionHandler.checkForNotificationPermission(this)) {
            createNotificationChannel();
            Notification notification = buildNotification();
            startForeground(NOTIFICATION_ID, notification);
        } else {
            Log.e("TAG", "Cannot send notification, since it was not granted!");
        }
    }

    /**
     * Sets the notification channel
     */
    private void createNotificationChannel() {
        CharSequence channelName = "GPS";
        String channelDescription = "Service running in background";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
        channel.setDescription(channelDescription);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    /**
     * Implements notification that will be shown if a GPS track is running
     * Has a title, text, icon, cannot be deleted and opens GPSFragment when beeing clicked
     *
     * @return Notification with config
     */
    private Notification buildNotification() {
        Context context = getApplicationContext();
        Bitmap largeIconBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo);

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("gpsFragmentOpen", "gpsTracking");
        intent.setAction("OPEN_FRAGMENT");
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(getString(R.string.gps_service_notification_title))
                .setContentText(getString(R.string.gps_service_notification_description)) // 46 letters on S10
                .setSmallIcon(R.drawable.logo)
                .setLargeIcon(largeIconBitmap)
                .setColor(Config.GPS_NOTIFICATION_COLOR)
                .setOngoing(Config.GPS_NOTIFICATION_IS_ONGOING)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        return builder.build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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
     * Starts the tracking timer and increases variable each second 1000ms
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
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTracking();
    }
}
