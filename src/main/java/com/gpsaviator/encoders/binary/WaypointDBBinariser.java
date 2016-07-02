package com.gpsaviator.encoders.binary;

import android.util.Log;
import com.gpsaviator.CoordinateFactory;
import com.gpsaviator.Waypoint;
import com.gpsaviator.WaypointDB;
import com.gpsaviator.encoders.Binariser;

import java.nio.ByteBuffer;
import java.util.Collections;

/**
 * Created by khaines on 11/12/14.
 */

public class WaypointDBBinariser implements Binariser<WaypointDB> {

    private static final String TAG = "WPDBBinariser";
    private final WaypointBinariser wpb;

    public WaypointDBBinariser(CoordinateFactory coordinateFactory) {
        wpb = new WaypointBinariser(coordinateFactory);
    }

    @Override
    public int byteSize(WaypointDB db) {
        int total = Integer.SIZE / 8; // for record header - num of waypoints
        for (Waypoint waypoint : db.getWaypoints()) {
            total += wpb.byteSize(waypoint);
        }
        return total;
    }

    @Override
    public WaypointDB fromBuffer(ByteBuffer bb) {
        int count = bb.getInt();
        WaypointDB db = new WaypointDB(count);
        Log.d(TAG, "Loading " + count + " waypoints");
        for (int j = 0; j < count; j++) {
            db.add(wpb.fromBuffer(bb));
        }
        Log.d(TAG, "Completed");
        db.sort();
        return db;
    }

    @Override
    public void toBuffer(WaypointDB db, ByteBuffer bb) {
        bb.putInt(db.getWaypoints().size());
        for (Waypoint waypoint : db.getWaypoints()) {
            wpb.toBuffer(waypoint, bb);
        }
    }
}
