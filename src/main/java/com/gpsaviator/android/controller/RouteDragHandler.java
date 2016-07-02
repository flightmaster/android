package com.gpsaviator.android.controller;

import android.util.Log;
import android.widget.ArrayAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.SphericalUtil;
import com.gpsaviator.Route;
import com.gpsaviator.android.model.CoordinateImpl;
import com.gpsaviator.android.model.MapRoute;
import lombok.Getter;

import java.util.List;

/**
 * Created by khaines on 03/01/2015.
 */
public class RouteDragHandler implements DragHandler.DragHandlerSub {

    private static final int DRAG_RENAME_THRESHOLD = 7000; // distance (m) user must drag a waypoint before its renamed
    private static final String LOGTAG = "ROUTEDRAG";

    private List<LatLng> linePoints;

    @Getter
    private LatLng startLatLng;
    private boolean movedMarker = false;

    private final Selection selection;

    @Getter
    private int markerIndexToRoute;

    private MapRoute route;
    private Runnable runnable;

    public RouteDragHandler(MapRoute mapRoute, Selection selection, Runnable runnable) {
        this.route = mapRoute;
        this.selection = selection;
        if (selection != null) {
            selection.clear();
        }
        this.runnable = runnable;
    }

    @Override
    public void start(Marker marker) {
        Log.d(LOGTAG, "Route drag start");
        linePoints = route.getLine().getPoints();
        if ((markerIndexToRoute = route.getMarkerIndex(marker)) == -1) {
            throw new IllegalStateException("Marker not found in route");
        }
        selection.clear();
        Selection.legWaypoint(route, markerIndexToRoute);

        startLatLng = linePoints.get(markerIndexToRoute);
        movedMarker = false;
//        selection = Selection.legWaypoint(route, getMarkerIndexToRoute());
    }

    @Override
    public void drag(Marker marker) {
        movedMarker = true;
        linePoints.set(markerIndexToRoute, marker.getPosition());
        route.getLine().setPoints(linePoints);
        route.getRoute().movePoint(CoordinateImpl.createCoordinateImpl(marker.getPosition()), markerIndexToRoute);
    }

    @Override
    public void end(Marker marker) {
        Log.d(LOGTAG, "Route drag end");
        if (!movedMarker) {
            linePoints.set(markerIndexToRoute, startLatLng);
            route.getLine().setPoints(linePoints);
            marker.setPosition(startLatLng);
            return;
        }
        /*
         * update the route to match the final position of the marker.
         */
        route.getRoute().movePoint(CoordinateImpl.createCoordinateImpl(marker.getPosition()), markerIndexToRoute);

        // Update the name of the marker if it moved far
        if (SphericalUtil.computeDistanceBetween(marker.getPosition(), getStartLatLng()) > DRAG_RENAME_THRESHOLD) {
            BackgroundGeocoder.getInstance().start(marker.getPosition(), marker.getTitle(), runnable);
        }

    }
}
