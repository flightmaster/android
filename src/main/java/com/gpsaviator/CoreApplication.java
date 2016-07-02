package com.gpsaviator;

/* 
 * Provides an abstraction layer to allow the UI to communicate 
 * with the underlying application logic. The application logic must be
 * implemented via this class.
 *
 *  TODO: * Remove CoreApplication, it's an unecessary abstraction. Use more local
 *  classes to implement these functions. There won't really be a way to compose a framework
 *  that works across all OS's, so focus on providing reusable libraries and components
 *
 */

import android.os.DeadObjectException;
import android.util.Log;
import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public abstract class CoreApplication {

	public static final int WAYPOINT_NOT_SELECTED = -1;
	public Route route = null;
	public LocationData location = null;
	public WaypointDB wpdb = null;
	public AirspaceDB asdb = null;
	private int selectedWaypointID;

	public EventBus eventBus = new EventBus();

	@Subscribe
	private void deadEvent(DeadEvent deadEvent) {
		Log.d("CORE", "dead event " + deadEvent.toString());
	}

	protected CoreApplication() {
		super();
		route = Route.create();
		wpdb = new WaypointDB(100);
		asdb = new AirspaceDB(100);
		eventBus.register(this);
	}

	public abstract void updateLocation(LocationData newLocation);

	public abstract void loadRoute();

	public abstract void saveRoute();

	public abstract void loadWaypointDB();

	public abstract void saveWaypointDB();

	public abstract void loadAirspaceDB();

	public int getSelectedWaypointID() {
		return selectedWaypointID;
	}

	public void setSelectedWaypointID(int id) {
		selectedWaypointID = id;
	}

}
