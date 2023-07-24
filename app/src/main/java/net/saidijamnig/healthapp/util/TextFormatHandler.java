package net.saidijamnig.healthapp.util;

import net.saidijamnig.healthapp.Config;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * The TextFormatHandler class provides utility methods for formatting text in the application.
 */
public class TextFormatHandler {

    // Private constructor to prevent instantiation of the class
    private TextFormatHandler() {
        // No instantiation of class
    }

    /**
     * Formats the duration time in milliseconds into a specific format.
     *
     * @param elapsedDurationTimeInMilliSeconds The elapsed duration time in milliseconds.
     * @param unformattedDuration The unformatted duration format string.
     * @return The formatted duration time.
     */
    public static String getFormattedDurationTime(int elapsedDurationTimeInMilliSeconds, String unformattedDuration) {
        // Calculate the hours, minutes, and seconds from the elapsed duration time
        int hours = (elapsedDurationTimeInMilliSeconds / (1000 * 60 * 60)) % 24;
        int minutes = (elapsedDurationTimeInMilliSeconds / (1000 * 60)) % 60;
        int seconds = (elapsedDurationTimeInMilliSeconds / 1000) % 60;

        // Format the duration time using the unformatted duration format string
        return String.format(unformattedDuration, formatTime(hours), formatTime(minutes), formatTime(seconds));
    }

    /**
     * Formats the given value as a two-digit string with a leading zero if necessary.
     *
     * @param value The value to be formatted.
     * @return The formatted value as a string.
     */
    private static String formatTime(int value) {
        return String.format(Locale.getDefault(), Config.DURATION_FORMAT, value);
    }

    /**
     * Formats the current date with a specific format.
     *
     * @param isImageTrackName Determines if the formatted date is for an image track name.
     * @param currentDate The current date to be formatted.
     * @return The formatted date as a string.
     */
    public static String formatCurrentDate(boolean isImageTrackName, Date currentDate) {
        String formattedDate;

        // Determine the format based on whether it is for an image track name or not
        if (!isImageTrackName) {
            formattedDate = Config.TIME_FORMAT_GENERAL;
        } else {
            formattedDate = Config.TIME_FORMAT_TRACK_NAME;
        }

        // Create a SimpleDateFormat object with the specified format and locale
        SimpleDateFormat dateFormat = new SimpleDateFormat(formattedDate, Locale.getDefault());

        // Format the current date using the SimpleDateFormat object
        return dateFormat.format(currentDate);
    }
}