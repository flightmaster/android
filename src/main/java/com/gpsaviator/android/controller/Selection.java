package com.gpsaviator.android.controller;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.gpsaviator.Waypoint;
import com.gpsaviator.android.model.MapRoute;
import lombok.Getter;

/**
* Created by khaines on 13/06/14.
*/

public class Selection {

    private static Selection instance = new Selection();

    private static SelectedItem item = null;

    Selection() {
        item = null;
    }

    public static void waypoint(Waypoint wp, Marker marker) {
        item = new SelectedWaypoint(marker, wp);
    }

    public static void legWaypoint(MapRoute fp, int wpNumber) {
        item = new SelectedRouteWaypoint(fp.getMarker(wpNumber), wpNumber);
    }

    public static void freePoint(Marker marker) {
        item = new SelectedFreePoint(marker);
    }

    public LatLng getLatLng() {
        return item.getLatLng();
    }

    public Marker getMarker() {
        return item.getRelatedMarker();
    }

    public boolean isRouteWaypoint() {
        return (getType() == SelectionType.ROUTE_WAYPOINT);
    }

    public int getWaypointNumber() {
        if (getType() != SelectionType.ROUTE_WAYPOINT) {
            throw new IllegalStateException("Invalid selection state:" + getType().toString());
        }
        return ((SelectedRouteWaypoint)item).getIdx();
    }

    public Waypoint getWaypoint() {
        if (getType() != SelectionType.WAYPOINT) {
            throw new IllegalStateException("Invalid selection state:" + getType().toString());
        }
        return ((SelectedWaypoint) item).getWaypoint();
    }

    public void clear() {
        if (item == null) {
            return;
        }

        if (item.getRelatedMarker() != null) {
            item.getRelatedMarker().hideInfoWindow();
            if (!isRouteWaypoint() && !isWaypoint()) {
                item.getRelatedMarker().remove();
            }
        }
        item = null;
    }

    public boolean isWaypoint() {
        return getType() == SelectionType.WAYPOINT;
    }

    public boolean isFreePoint() {
        return isSelected() && !(isRouteWaypoint() || isWaypoint());
    }

    public static Selection getInstance() {
        return instance;
    }

    public enum SelectionType {
        NONE, WAYPOINT, ROUTE_WAYPOINT, FREEPOINT
    }

    public SelectionType getType() {
        return item == null ? SelectionType.NONE : item.getType();
    }

    public boolean isSelected() {
        return getType() != SelectionType.NONE;
    }

    private static abstract class SelectedItem {
        protected final Marker relatedMarker;
        abstract public SelectionType getType();

        SelectedItem(Marker marker) {
            relatedMarker = marker;
            relatedMarker.showInfoWindow();
        }

        public Marker getRelatedMarker() {
            return relatedMarker;
        }
        public LatLng getLatLng() {
            return relatedMarker.getPosition();
        }
    }

    private static class SelectedWaypoint extends SelectedItem {
        private final Waypoint waypoint;

        SelectedWaypoint(Marker marker, Waypoint waypoint) {
            super(marker);
            this.waypoint = waypoint;
        }

        @Override
        public SelectionType getType() {
            return SelectionType.WAYPOINT;
        }

        public Waypoint getWaypoint() {
            return waypoint;
        }
    }

    private static class SelectedRouteWaypoint extends SelectedItem {
        private final int wpIndex;

        SelectedRouteWaypoint(Marker marker, int wpNumber) {
            super(marker);
            this.wpIndex = wpNumber;
        }

        @Override
        public SelectionType getType() {
            return SelectionType.ROUTE_WAYPOINT;
        }

        public int getIdx() {
            return wpIndex;
        }
    }

    private static class SelectedFreePoint extends SelectedItem {
        public SelectedFreePoint(Marker marker) {
            super(marker);
        }

        @Override
        public SelectionType getType() {
            return SelectionType.FREEPOINT;
        }
    }
}
