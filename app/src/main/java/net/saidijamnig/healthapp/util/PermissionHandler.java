package net.saidijamnig.healthapp.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * This class provides utility methods for managing permissions in an Android application.
 */
public class PermissionHandler {
    /**
     * Request code for location permission.
     */
    public static final int REQUEST_LOCATION_PERMISSION = 1;
    public static final int REQUEST_BODY_SENSOR_PERMISSION = 1;
    public static final int REQUEST_ACTIVITY_RECOGNITION_PERMISSION = 1;

    /**
     * Private constructor to prevent instantiation of the PermissionManager class.
     */
    private PermissionHandler() {
        // Prevent instantiation
    }

    /**
     * Checks if the required location permissions are granted.
     *
     * @param context The context of the application.
     * @return True if the required location permissions are granted, false otherwise.
     */
    public static boolean checkForRequiredPermissions(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Requests location permissions from the user.
     *
     * @param activity The activity requesting the permissions.
     */
    public static void requestGpsPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
    }

    /**
     * Checks if the notification permission is granted.
     *
     * @param context The context of the application.
     * @return True if the notification permission is granted, false otherwise.
     */
    public static boolean checkForNotificationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkForBodySensorPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestBodySensorPermission(Activity activityCompat) {
        ActivityCompat.requestPermissions(activityCompat, new String[]{Manifest.permission.BODY_SENSORS}, REQUEST_BODY_SENSOR_PERMISSION);
    }

    public static boolean checkForActivityRecognitionPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestActivityRecognitionPermission(Activity activityCompat) {
        ActivityCompat.requestPermissions(activityCompat, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, REQUEST_ACTIVITY_RECOGNITION_PERMISSION);
    }
}
