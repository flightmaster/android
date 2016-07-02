package com.gpsaviator.android;

import android.os.Handler;
import android.util.Log;
import com.gpsaviator.*;
import com.gpsaviator.android.Storage.StorageType;
import com.gpsaviator.android.model.CoordinateImplFactory;
import com.gpsaviator.encoders.binary.AirspaceDBBinariser;
import com.gpsaviator.encoders.binary.WaypointDBBinariser;
import com.gpsaviator.encoders.json.AirspaceDBStream;
import com.gpsaviator.encoders.json.CoordinateJsonsiser;
import com.gpsaviator.encoders.json.RouteJsoniser;
import com.gpsaviator.encoders.json.WaypointDBStream;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * Provides an implementation of AviatorInterface using process-level
 * interactions, i.e. all "communication" with the application logic is being
 * handled locally within the process via private member variables.
 * 
 */

public class CoreAndroid extends CoreApplication {

	private static final String WAYPOINTS_FILE = "waypoints.dat";
	private static final String FLIGHTPLAN_FILE = "flightplan.dat";
    private static final String AIRSPACE_FILE = "airspace.dat";
    private static final String TAG = "CORE";

    private final AviatorApp app;
	private static final String AIRSPACE_BIN_FILE = "airspace_bin.dat";
    private final String WAYPOINTS_BIN_FILE = "waypoints_bin.dat";

    private Thread dbThread;

    public CoreAndroid(AviatorApp app) {
        super();
        this.app = app;

        final Handler handler = new Handler();

        dbThread = new Thread() {
            @Override
            public void run() {
                loadWaypointDB();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        eventBus.post(wpdb);
                    }
                });
                loadAirspaceDB();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        eventBus.post(asdb);
                    }
                });
            }
        };
        dbThread.start();
        loadRoute();
    }

    private boolean loadJson = false;

    private Integer waypointsJsonId = loadJson ? R.raw.waypoints_json : null;
    private Integer airspaceJsonId = loadJson ?  R.raw.airspace_json : null;

    @Override
	public void loadWaypointDB() {
        final WaypointDBBinariser dbBinariser = new WaypointDBBinariser(CoordinateImplFactory.getInstance());
        Storage<WaypointDB> s = new Storage<WaypointDB>();

        if (waypointsJsonId != null) {
            Log.d("LOAD", "Loading waypoints from JSON");
            WaypointDBStream stream = new WaypointDBStream(wpdb, CoordinateImplFactory.getInstance());
            Storage.importFromResource(app, stream, waypointsJsonId);
        } else {
            wpdb = s.readBinary(app, R.raw.waypoints_bin, dbBinariser);
        }
    }

	@Override
	public void loadAirspaceDB() {
        Storage<AirspaceDB> s = new Storage<AirspaceDB>();
        final AirspaceDBBinariser dbBinariser = new AirspaceDBBinariser(CoordinateImplFactory.getInstance());
        if (airspaceJsonId != null) {
            Log.d("LOAD", "Loading airspace from JSON");
            AirspaceDBStream stream = new AirspaceDBStream(asdb, CoordinateImplFactory.getInstance());
            Storage.importFromResource(app, stream, airspaceJsonId);
            s.writeBinary(app, AIRSPACE_BIN_FILE, asdb, dbBinariser);
        } else {
            asdb = s.readBinary(app, R.raw.airspace_bin, dbBinariser);
        }
        eventBus.post(asdb);
    }

	public void saveWaypointDB() {
        final WaypointDBBinariser dbBinariser = new WaypointDBBinariser(CoordinateImplFactory.getInstance());
        Storage<WaypointDB> s = new Storage<WaypointDB>();
        s.writeBinary(app, WAYPOINTS_BIN_FILE, wpdb, dbBinariser);
	}

	@Override
	public void updateLocation(LocationData newLocation) {
	}

	@Override
	public void loadRoute() {
        RouteJsoniser rj = new RouteJsoniser(new CoordinateJsonsiser(CoordinateImplFactory.getInstance()));
        try {
            JSONObject json = Storage.importFromSD(app, StorageType.INTERNAL, FLIGHTPLAN_FILE);
            route = rj.fromJson(json);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "Unable to read route JSON, leaving it empty");
            route = Route.create();
//            throw new RuntimeException("Unable to read route JSON");
        } catch (RuntimeException e) {
            e.printStackTrace();
            Log.d(TAG, "Unable to read route JSON, leaving it empty");
            route = Route.create();
//            throw new RuntimeException("Unable to read route JSON");
        }
    }

	@Override
	public void saveRoute() {
        RouteJsoniser rj = new RouteJsoniser(new CoordinateJsonsiser(CoordinateImplFactory.getInstance()));
        JSONObject json = null;
        try {
            json = rj.toJson(route);
            Storage.exportToSD(app, json, StorageType.INTERNAL, FLIGHTPLAN_FILE);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to save route");
        }
    }
}
