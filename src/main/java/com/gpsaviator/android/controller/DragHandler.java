package com.gpsaviator.android.controller;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by khaines on 03/01/2015.
 *
 * There will eventually be more drag handlers depending on the type of marker
 * being dragged, so this class will change to handle that.
 *
 */
public class DragHandler implements GoogleMap.OnMarkerDragListener {

    private DragHandlerSub handler;
    private MainActivity mainActivity;

    public interface DragHandlerSub {
        public void start(Marker marker);
        public void drag(Marker marker);
        public void end(Marker marker);
    }

    public DragHandler(MainActivity mainActivity) {
    }

    public void setHandler(DragHandlerSub handler) {
        this.handler = handler;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        handler.start(marker);
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        handler.drag(marker);
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        handler.end(marker);
    }
}
