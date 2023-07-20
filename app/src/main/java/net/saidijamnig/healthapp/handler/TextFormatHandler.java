package net.saidijamnig.healthapp.handler;

import net.saidijamnig.healthapp.Config;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TextFormatHandler {
    private TextFormatHandler() {
        // No instantiation of class
    }

    public static String getFormattedDurationTime(int elapsedDurationTimeInMilliSeconds, String unformattedDuration) {
        int hours = (elapsedDurationTimeInMilliSeconds / (1000 * 60 * 60)) % 24;
        int minutes = (elapsedDurationTimeInMilliSeconds / (1000 * 60)) % 60;
        int seconds = (elapsedDurationTimeInMilliSeconds / 1000) % 60;

        return String.format(unformattedDuration, formatTime(hours), formatTime(minutes), formatTime(seconds));
    }

    private static String formatTime(int value) {
        return String.format(Locale.getDefault(), Config.DURATION_FORMAT, value); // two digits and the leading is a zero if necessary
    }

    /**
     * Formats the currentDate with a specific format
     *
     * @param isImageTrackName - Determines if it should be generated for an image track name
     * @return formatted date
     */
    public static String formatCurrentDate(boolean isImageTrackName, Date currentDate) {
        String formattedDate;

        if (!isImageTrackName) formattedDate = Config.TIME_FORMAT_GENERAL;
        else formattedDate = Config.TIME_FORMAT_TRACK_NAME;

        SimpleDateFormat dateFormat = new SimpleDateFormat(formattedDate, Locale.getDefault());
        return dateFormat.format(currentDate);
    }
}
