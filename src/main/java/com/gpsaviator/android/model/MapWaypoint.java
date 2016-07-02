package com.gpsaviator.android.model;

import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.Marker;
import lombok.Getter;

/**
 * Created by khaines on 15/06/14.
 */
public class MapWaypoint {
    @Getter
    private final Marker marker;

    @Getter
    private final GroundOverlay overlay;

    public MapWaypoint(Marker marker) {
        this.marker = marker;
        this.overlay = null;
    }

    public MapWaypoint(Marker marker, GroundOverlay groundOverlay) {
        this.marker = marker;
        this.overlay = groundOverlay;
    }

    public void remove() {
        marker.remove();
        if (overlay != null) {
            overlay.remove();
        }
    }

}
