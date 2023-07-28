package net.saidijamnig.healthapp;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

/**
 * Configuration class containing constant values used throughout the application.
 * These values include file paths, date formats, map settings, image compression options,
 * and other configuration-related constants.
 */
public class Config {

    // Image-related constants
    public static final int FALLBACK_IMAGE_PATH = R.drawable.no_image;
    public static final String TRACK_NAME_FORMAT = "activity_track_%s";

    // Duration and distance format constants
    public static final String DURATION_FORMAT = "%02d"; // Int (2 digits and a leading 0 if necessary)
    public static final String DISTANCE_FORMAT = "%.02f"; // Double (two digits after comma and a leading 0 if necessary)

    // Map-related constants
    public static final float GENERAL_CAMERA_ZOOM = 16f; // the higher the nearer
    public static final float DEFAULT_MARKER_COLOR = BitmapDescriptorFactory.HUE_RED;
    public static final String TIME_FORMAT_TRACK_NAME = "ddMMyyyy_HHmmss";
    public static final String TIME_FORMAT_GENERAL = "dd-MM-yyyy HH:mm:ss";
    public static final String TIME_FORMAT_HEALTH = "yyyy_MM_dd";
    public static final float MAP_LINE_WIDTH = 10f;
    public static final int MAP_LINE_COLOR = Color.BLUE;
    public static final int COMPRESS_QUALITY = 90;
    public static final Bitmap.CompressFormat COMPRESS_FORMAT = Bitmap.CompressFormat.PNG;
    public static final long MAP_UPDATE_INTERVAL = 1000L; // Determines the frequency of location updates
    // Notification Builder
    public static final int GPS_NOTIFICATION_COLOR = Color.BLUE;
    public static final boolean GPS_NOTIFICATION_IS_ONGOING = true;
    // Health Page
    public static final int MAX_CALORIES_LENGTH = 6;
    public static final int MAX_CALORIES_AMOUNT = 100000000;

    public static final int NAVBAR_HEIGHT = 200; // Used to determine padding for recycler view entry

    // Compass tracking settings
    public static final int COMPASS_TRACKING_UPDATE_INTERVAL = 0; // in ms
    public static final int COMPASS_TRACKING_MIN_UPDATE_DISTANCE = 0; // in meter

    private Config() {
        // Private constructor to prevent instantiation
    }
}