package net.saidijamnig.healthapp;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

public class Config {
    public static final int FALLBACK_IMAGE_PATH = R.drawable.no_image;
    public static final String TRACK_NAME_FORMAT = "activity_track_%s";
    public static final String DURATION_FORMAT = "%02d"; // Int
    public static final String DISTANCE_FORMAT = "%.02f"; // Double
    public static final float GENERAL_CAMERA_ZOOM = 15f;
    public static final float DEFAULT_MARKER_COLOR = BitmapDescriptorFactory.HUE_RED;
    public static final String TIME_FORMAT_TRACK_NAME = "ddMMyyyy_HHmmss";
    public static final String TIME_FORMAT_GENERAL = "dd-MM-yyyy HH:mm:ss";
    public static final String TIME_FORMAT_HEALTH = "yyyy_MM_dd";
    public static final float MAP_LINE_WIDTH = 10f;
    public static final int MAP_LINE_COLOR = Color.BLUE;
    public static final int COMPRESS_QUALITY = 90;
    public static final Bitmap.CompressFormat COMPRESS_FORMAT = Bitmap.CompressFormat.PNG;
    public static final long MAP_UPDATE_INTERVAL = 3000L;
    public static final int GPS_NOTIFICATION_COLOR = Color.BLUE;
    public static final boolean GPS_NOTIFICATION_IS_ONGOING = true;
    public static final int NAVBAR_HEIGHT = 200; // Used to determine padding for recycler view entry
    public static final int MAX_CALORIES_LENGTH = 6;
    public static final int COMPASS_TRACKING_UPDATE_INTERVAL = 0; // in ms
    public static final int COMPASS_TRACKING_MIN_UPDATE_DISTANCE = 0; // in meter

    private Config() {
        // No instantiation
    }
}