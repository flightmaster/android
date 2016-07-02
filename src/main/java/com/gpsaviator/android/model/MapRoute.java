package com.gpsaviator.android.model;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.*;
import com.gpsaviator.Route;
import lombok.Getter;

import java.util.ArrayList;

import static com.gpsaviator.android.model.CoordinateImpl.asLatLng;

/**
 * Managing the map representation of a flight plan on a Google Map.
 */
public class MapRoute {

	@Getter
	private  Polyline line = null;

	@Getter
	private ArrayList<Marker> markers = null;

    @Getter
    private Route route;

    private final GoogleMap map;

    /**
     * Adds a flight plan to the specified map.
     * @param map
     * @param route
     */
	public MapRoute(GoogleMap map, Route route) {
        this.route = route;
        this.map = map;
        update();
	}

    public void setRoute(Route fp) {
        route = fp;
        update();
    }
    /**
     * Update the map model view of the route, following an update to the route
     */
    public void update() {
        if (line != null) {
            line.remove();
            line = null;
        }
        if (markers != null) {
            for (Marker m:markers) {
                m.remove();
            }
            markers = null;
        }
        if (route.getNumPoints() == 0) {
            return;
        }
        PolylineOptions polylineOptions = new PolylineOptions();
        markers = new ArrayList<Marker>(route.getNumPoints());

        polylineOptions.add(asLatLng(route.getPoint(0).getCoord()));
        final int lastWpIdx = route.getNumPoints()-1;
        for (int i = 0; i < route.getNumPoints(); i++) {
            Route.RouteWaypoint wp = route.getPoint(i);
            LatLng ll = asLatLng(wp.getCoord());

            BitmapDescriptor iconColour;
            if (i == 0) {
                iconColour = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
            } else if (i == lastWpIdx) {
                iconColour = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
            } else {
                iconColour = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
            }
            MarkerOptions newMarker = new MarkerOptions()
                    .title(wp.getIdent())
                    .icon(iconColour)
                    .position(ll)
                    .draggable(true);
            markers.add(map.addMarker(newMarker));
            if (i > 0) {
                polylineOptions.add(ll);
            }
        }
        line = map.addPolyline(polylineOptions.geodesic(true));
    }

    public LatLngBounds getBounds() {

        if (getMarkers().size() == 0) return LatLngBounds.builder().include(new LatLng(0,0)).build();

        LatLngBounds bounds = LatLngBounds.builder().include(getMarkers().get(0).getPosition()).build();

        for (Marker m : getMarkers()) {
            bounds = bounds.including(m.getPosition());
        }
        return bounds;
    }

    /**
     * Test if the given marker is part of the flight plan markers model on the map.
     *
     * @param m The marker to test
     * @return True if the marker is part of the flight plan.
     */
    public boolean isPlanMarker(Marker m) {
        return markers.contains(m);
    }

    /**
     * Find the flight plan leg nearest the given point, and return the index to it.
     *
     * @param point Point to use as the reference.
     * @return Index to the nearest leg, or -1 if no leg was nearby.
     */
    public int getSelectedLeg(LatLng point) {
        return -1;
    }

    /**
     * Get the index of the given marker within the plan
     * @param marker
     * @return Index of marker, or -1 if not found
     */
    public int getMarkerIndex(Marker marker) {
        if (markers != null) {
            return getMarkers().indexOf(marker);
        }
        return -1;
    }

    /**
     * Get the marker for the given turn point number
     * @param num Marker to get
     * @return The marker
     */
    public Marker getMarker(int num) {
        if (num >= 0 && num < route.getNumPoints()) {
            return markers.get(num);
        }
        throw new IllegalArgumentException("Turn point out of range");
    }
}