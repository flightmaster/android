package com.gpsaviator.encoders.json;

import android.util.Log;
import com.gpsaviator.CoordinateFactory;
import com.gpsaviator.Waypoint;
import com.gpsaviator.WaypointDB;
import com.gpsaviator.encoders.JsonStreamable;
import lombok.Getter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by khaines on 31/01/2015.
 */
public class WaypointDBStream implements JsonStreamable {

    private static final String TAG = "WpDBStream";

    @Getter
    private WaypointDB wpdb;
    private WaypointJsoniser wj;

    public WaypointDBStream(WaypointDB wpdb, CoordinateFactory cf) {
        this.wpdb = wpdb;
        this.wj = new WaypointJsoniser(cf);
    }

    @Override
    public void readStream(InputStream is) throws IOException, JSONException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
        Log.d(TAG, "JSON read started");
		while (br.ready()) {
			String line = br.readLine();
            wpdb.add(wj.fromJson(new JSONObject(line)));
		}
        wpdb.sort();
		Log.d(TAG, "JSON read complete");
    }

    @Override
    public void writeStream(OutputStream os) throws IOException, JSONException {
		Log.d(TAG, "JSON write started");
        for (Waypoint waypoint : wpdb.getWaypoints()) {
            JSONObject json = wj.toJson(waypoint);
            os.write(json.toString().getBytes());
            os.write(10);
        }
        Log.d(TAG, "JSON write completed");
    }
}
