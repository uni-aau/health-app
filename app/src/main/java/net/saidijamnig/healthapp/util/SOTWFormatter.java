package net.saidijamnig.healthapp.util;

import android.content.Context;

import net.saidijamnig.healthapp.R;

/**
 * This class provides utility methods for formatting the wind direction based on the azimuth angle.
 */
public class SOTWFormatter {
    private static final int[] angles = {0, 45, 90, 135, 180, 225, 270, 315, 360};
    private static String[] directions = null;

    /**
     * Constructs a new SOTWFormatter object.
     *
     * @param context The context used to initialize the localized directions.
     */
    public SOTWFormatter(Context context) {
        if (directions == null) {
            initializeLocalizedDirections(context);
        }
    }

    /**
     * Finds the closest angle index to the target angle.
     *
     * @param target The target angle.
     * @return The index of the closest angle.
     */
    private static int findClosestAngleIndex(int target) {
        int i = 0;
        int j = angles.length - 1;
        int mid = 0;
        while (i < j) {
            mid = (i + j) / 2;

            if (target < angles[mid]) {
                if (mid > 0 && target > angles[mid - 1]) {
                    return getClosestIndex(mid - 1, mid, target);
                }
                j = mid;
            } else {
                if (mid < angles.length - 1 && target < angles[mid + 1]) {
                    return getClosestIndex(mid, mid + 1, target);
                }
                i = mid + 1;
            }
        }

        return mid;
    }

    /**
     * Gets the closest index between two angles based on the target angle.
     *
     * @param index1 The index of the first angle.
     * @param index2 The index of the second angle.
     * @param target The target angle.
     * @return The index of the closest angle.
     */
    private static int getClosestIndex(int index1, int index2, int target) {
        if (target - angles[index1] >= angles[index2] - target) {
            return index2;
        }
        return index1;
    }

    /**
     * Formats the azimuth angle and returns the formatted string.
     *
     * @param azimuth The azimuth angle.
     * @return The formatted string.
     */
    public String format(float azimuth) {
        int intAzimuth = (int) azimuth;
        int index = findClosestAngleIndex(intAzimuth);
        return intAzimuth + "° " + directions[index];
    }

    /**
     * Initializes the localized directions using the provided context.
     *
     * @param context The context used to retrieve the localized direction strings.
     */
    private void initializeLocalizedDirections(Context context) {
        directions = new String[]{
                context.getString(R.string.direction_north),
                context.getString(R.string.direction_northeast),
                context.getString(R.string.direction_east),
                context.getString(R.string.direction_southeast),
                context.getString(R.string.direction_south),
                context.getString(R.string.direction_southwest),
                context.getString(R.string.direction_west),
                context.getString(R.string.direction_northwest),
                context.getString(R.string.direction_north)
        };
    }
}