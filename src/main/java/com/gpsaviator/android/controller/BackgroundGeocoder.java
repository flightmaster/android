package com.gpsaviator.android.controller;

import android.location.Geocoder;
import android.os.Handler;
import com.google.android.gms.maps.model.LatLng;
import com.gpsaviator.android.WaypointNamer;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * Created by khaines on 04/01/2015.
 */
public class BackgroundGeocoder {

    private Thread namerThread = null;
    private WaypointNamer waypointNamer = null;
    private static final BackgroundGeocoder instance = new BackgroundGeocoder();
    private static Geocoder geo;

    private BackgroundGeocoder() {
        // Non-instantiable
    }

    public static BackgroundGeocoder getInstance() {
        if (geo == null) {
            throw new IllegalStateException("Background geocoder not initialised");
        }
        return instance;
    }

    public static void initialise(Geocoder geocoder) {
        geo = geocoder;
    }

    public void start(final LatLng position, final String defaultName, final Runnable runnable) {
        if (namerThread != null) {
            namerThread.interrupt();
        }
        if (waypointNamer != null) {
            waypointNamer = null;
        }

        final Handler mHandler = new Handler();

        namerThread = new Thread() {
            public void run() {
                WaypointNamer wn = WaypointNamer.position(position, geo, defaultName);
                if (!this.isInterrupted()) {
                    waypointNamer = wn;
                    mHandler.post(runnable);
                }
            }
        };
        namerThread.start();
    }

    public void stop() {
        if (namerThread != null) {
            namerThread.interrupt();
            namerThread = null;
        }
        waypointNamer = null;
    }

    public String getNextName() {
        if (waypointNamer != null) {
            return waypointNamer.getNextName();
        }
        return null;
    }

    public List<String> getNames() {
        if (waypointNamer != null && waypointNamer.getAllNames().size() > 0) {
            return waypointNamer.getAllNames();
        }
        return Collections.EMPTY_LIST;
    }
}
