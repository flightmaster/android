package com.gpsaviator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import com.gpsaviator.encoders.Binariser;
import com.gpsaviator.encoders.JsonStreamable;
import com.gpsaviator.encoders.json.WaypointJsoniser;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class WaypointDB {

    public static final String TAG = "WaypointDB";
    private ArrayList<Waypoint> waypoints = null, nameOrdered = null;

	public WaypointDB(int size) {
		waypoints = new ArrayList<Waypoint>(100);
	}

	public void sort() {
		Collections.sort(waypoints);
	}

	public int add(Waypoint wp) {
		waypoints.add(wp);
		nameOrdered = null;
		return waypoints.size() - 1;
	}

	/*
	 * modified binary search which returns the first matching element in the
	 * list rather than an arbitrary one.
	 */
	public int binarySearchDB(Waypoint wp) {
		return binarySearchDB(wp, null);
	}

	int binarySearchDB(Waypoint wp, Comparator<Waypoint> comparator) {

		if (comparator == null) {
			comparator = wp;
		}
		int pos = Collections.binarySearch(waypoints, wp, comparator);

		while (pos > 0 && comparator.compare(waypoints.get(pos - 1), wp) == 0) {
			pos--;
		}
		return pos;
	}

	public void deleteWaypoint(int id) {
		if (waypoints.size() > id) {
			waypoints.remove(id);
		}
		nameOrdered = null;
	}

	/*
	 * returns a 0 or positive result if the waypoint ident is found in the
	 * waypoint database, negative result otherwise (which if turned positive
	 * will indicate the insertion point of the supplied string)
	 */
	public int findByIdent(String ident) {

		Waypoint wp = new Waypoint(null, 0, 0, ident, "");
		int pos = binarySearchDB(wp, Waypoint.identComparator());
		return pos;
	}

	/*
	 * find the given name in the name-sorted database and return its index
	 * within that database. Returns negative result if not found (which if
	 * turned positive will indicate the insertion point of the supplied string)
	 */
	public int findByName(String name) {
		int result = binarySearchDB(new Waypoint(null, 0, 0, "", name), Waypoint.nameComparator());

		return result;
	}

	public Waypoint getWaypoint(int id) {
		if (waypoints.size() > id) {
			return new Waypoint(waypoints.get(id));
		} else {
			return null;
		}
	}

	public ArrayList<Waypoint> getWaypoints() {
		// TODO: This should return a copy of the list, or an immutable list
		return waypoints;
	}

	/*
	 * get array of waypoints sorted by latitude order
	 */
	public ArrayList<Waypoint> getWaypointsByLatitude() {

		return null;
	}

	/*
	 * get a copy of the waypoints DB ordered by the name field instead of the
	 * ident
	 */
	public ArrayList<Waypoint> getWaypointsByName() {
		if (nameOrdered == null) {
			nameOrdered = new ArrayList<Waypoint>(waypoints);

			Log.d(TAG, "Sort started");
			Collections.sort(nameOrdered, new Comparator<Waypoint>() {
                @Override
                public int compare(Waypoint w1, Waypoint w2) {
                    return w1.getName().compareToIgnoreCase(w2.getName());
                }
            });

			Log.d(TAG, "Sort completed");
			Waypoint wp2 = waypoints.get(6072);
			Log.d(TAG, Integer.toString(nameOrdered.indexOf(wp2)));
		}
		return nameOrdered;
	}

	public int identIDtoNameID(int wpID) {
		ArrayList<Waypoint> byNames = getWaypointsByName();
		Waypoint wp = waypoints.get(wpID);
		Log.d(TAG, wp.getIdent() + Integer.toString(byNames.indexOf(wp)));
		return byNames.indexOf(wp);
	}

	public void setWaypoint(int id, Waypoint wp) {
		if (waypoints.size() > id) {
			waypoints.set(id, wp);
		} else {
            throw new IllegalArgumentException("Waypoint ID index exceeded");
        }
        Collections.sort(waypoints);
		nameOrdered = null;
	}


    public Collection<Waypoint> getWithinBounds(Bounds bounds, int waypointLimit) {
        return bounds.filter(waypoints);
    }

}
