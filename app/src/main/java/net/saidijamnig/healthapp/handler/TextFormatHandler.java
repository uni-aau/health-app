package net.saidijamnig.healthapp.handler;

import net.saidijamnig.healthapp.Config;

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
}
