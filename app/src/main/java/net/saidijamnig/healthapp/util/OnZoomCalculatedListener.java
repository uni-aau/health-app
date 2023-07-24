package net.saidijamnig.healthapp.util;

import net.saidijamnig.healthapp.fragments.GpsFragment;

/**
 * Interface definition for a callback to be invoked when the zoom level is calculated for Google Maps.
 * This interface is used in conjunction with the {@link GpsFragment} to determine the appropriate zoom level
 * for displaying the tracked route on the map.
 */
public interface OnZoomCalculatedListener {

    /**
     * Called when the zoom level for Google Maps is calculated.
     * This method is invoked after determining the appropriate zoom level to fit the entire tracked route on the map.
     */
    void onZoomCalculated();
}

