package com.gpsaviator.android.controller;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.GeomagneticField;
import android.location.*;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.*;
import com.google.common.eventbus.Subscribe;
import com.gpsaviator.*;
import com.gpsaviator.android.*;
import com.gpsaviator.android.model.*;
import lombok.Getter;

import static com.gpsaviator.UnitConversion.*;
import static com.gpsaviator.android.model.CoordinateImpl.asLatLng;

public class MainActivity extends Activity implements LocationListener, OnClickListener, View.OnLongClickListener,
        OnCameraChangeListener, GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, GoogleMap.OnMapLoadedCallback,
        GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerDragListener

{
    public static final String LOGTAG = "MapActivity";

    private static final Converter metresToFeet = getConverter(Unit.METRES, Unit.FEET);
    private static final Converter metresPerSecondToKnots = getConverter(Unit.METRES, Unit.SECONDS, Unit.NAUTICAL_MILES, Unit.HOURS);


    // TODO: * Convert to resources
    private static final int AIRSPACE_LIMIT = 200;
    private static final int WAYPOINT_LIMIT = 300;
    private static final float ZOOM_AIRSPACE_CUTOFF = (float) 8.0;
    private static final float ZOOM_OVERLAY_CUTOFF = (float) 8.0;
    public static final double ZOOM_WAYPOINT_CUTOFF = (float) 8.0;
    public static final float VOR_RADIUS = 9260f;
    public static final float ATZ_RADIUS = 3704f;
    private final String aircraftMarkerTitle = "_aircraft";

    private CoreApplication ai;
    private LocationManager locationMgr;
    private GoogleMap map = null;
    private MapRoute mapRoute = null;

    private Selection selection = Selection.getInstance();

    private Marker aircraft;    // position of aircraft overlay
    private final LatLng ukCentre = new LatLng(54.136696, -3.22998);
    private Float reportedTrack = 234f;
    private LatLng reportedLocation = null;
    private TrackMode trackMode = TrackMode.FREE;
    private AirspaceTask airspaceTask = null;
    private MapElements<Airspace, MapAirspace> airspaceElements = null;
    private BlockingQueue<Airspace> airspaceQueue = null;

    // 3 classes of waypoints are displayed. Normal waypoints, which come from the
    // system database, user waypoints which are user-created/managed, and mapRoute waypoints
    // which are created from every non-system/user waypoint used in the users routes

    // TODO: * userwaypoints and mapRoute waypoints databases.
    private MapElements<Waypoint, MapWaypoint> waypointElements = null;
    private MapElements<Waypoint, MapWaypoint> userWaypointElements = null;
    private MapElements<Waypoint, MapWaypoint> routeWaypointElements = null;
    private Route undoRoute = null;
    private Toast clipToast;
    private CountDownTimer clipToastTimer = null;

    private final Handler handler = new Handler();

    private RouteDragHandler dragHandler;
    // Runnable to execute when BackgroundGeocoding has completed (for selection and drag operations).
    final Runnable updateInfoWindowRunnable = new Runnable() {
        @Override
        public void run() {
            updateSelectedMarkerTitle(BackgroundGeocoder.getInstance().getNextName());
        }
    };
    private boolean menuState = false;

    private PlanList planWindowController;
    private long lastCameraUpdateTime = 0;

    /**
     * Allows the user to start the process of changing a mapRoute waypoint's name.
     *
     * @param marker
     */
    @Override
    public void onInfoWindowClick(final Marker marker) {
        if (!selection.isSelected()) {
            Log.e(LOGTAG, "Info window clicked but no selection available");
            return;
        }

        if (selection.isRouteWaypoint()) {
            new EditWaypointDialog(this, BackgroundGeocoder.getInstance().getNames(), marker.getTitle(), new EditWaypointDialog.EditWaypointDialogOkClick() {
                @Override
                public void okClicked(String editText) {
                    updateSelectedMarkerTitle(editText);
                }
            });
        }
    }

    private void updateSelectedMarkerTitle(String newIdent) {
        if (!selection.isSelected()) {
            return;
        }
        final Marker marker = selection.getMarker();
        marker.setTitle(newIdent);
        marker.hideInfoWindow();
        marker.showInfoWindow();

        // Rename the associated mapRoute waypoint if the marker is related to it.
        if (mapRoute != null && selection.isRouteWaypoint()) {
            final int waypointNumber = selection.getWaypointNumber();
            Route.RouteWaypoint old = mapRoute.getRoute().getPoint(waypointNumber);
            mapRoute.getRoute().updatePoint(Route.RouteWaypoint.make(old.getCoord(), old.getMagVar(), newIdent, old.getNotes()), waypointNumber);
            ai.saveRoute();
            planWindowController.updateRoute(ai.route);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.clearRoute:
                undoRoute = ai.route;
                ai.route = Route.create();
                mapRoute.setRoute(ai.route);
                ai.saveRoute();
                ai.eventBus.post(ai.route);
                return true;

            case R.id.undoClear:
                if (undoRoute != null) {
                    Route swap = ai.route;
                    ai.route = undoRoute;
                    mapRoute.setRoute(ai.route);
                    if (swap.getNumPoints() > 0) {
                        undoRoute = swap;
                    } else {
                        undoRoute = null;
                    }
                    ai.saveRoute();
                    ai.eventBus.post(ai.route);
                } else {
                    Toast.makeText(getApplicationContext(), "Nothing to Undo", Toast.LENGTH_SHORT).show();
                }
                return true;

            default:
                break;
        }
        return false;
    }

    private enum TrackMode {
        FREE(R.string.freeMode),
        TRACK(R.string.trackMode),
        TRACKUP(R.string.trackUpMode);

        @Getter
        private final int buttonLabelId;

        TrackMode(int id) {
            buttonLabelId = id;
        }

        TrackMode next() {
            int o = ordinal();
            if (o == values().length - 1) {
                return values()[0];
            }
            return values()[o + 1];
        }

    }

    private void toggleMenu(boolean state) {
        int newVis = state ? View.VISIBLE : View.INVISIBLE;
        animateView(findViewById(R.id.mainMenu), state ? R.anim.in_from_bottom : R.anim.out_bottom, newVis);
        animateView(findViewById(R.id.planWindowView), state ? R.anim.in_from_left : R.anim.out_left, newVis);
    }

    // TODO: Refactor into classes, including a mapRoute-manager controller
    @Override
    public void onClick(View v) {
        final Route fp = mapRoute.getRoute();
        switch (v.getId()) {
            case R.id.menuButton:
                menuState = !menuState;
                toggleMenu(menuState);
                break;

            case R.id.clearRoute:
                Toast.makeText(getApplicationContext(), "Long Press to Clear Route", Toast.LENGTH_SHORT).show();
                break;

            case R.id.undoClear:
                if (undoRoute == null) {
                    Toast.makeText(getApplicationContext(), "Nothing to Undo", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Long press to Undo", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.satViewToggle:
                if (map != null) {
                    int mapType = map.getMapType();
                    switch (mapType) {
                        case GoogleMap.MAP_TYPE_NORMAL:
                            mapType = GoogleMap.MAP_TYPE_TERRAIN;
                            break;
                        case GoogleMap.MAP_TYPE_TERRAIN:
                            mapType = GoogleMap.MAP_TYPE_HYBRID;
                            break;
                        case GoogleMap.MAP_TYPE_SATELLITE:
                            mapType = GoogleMap.MAP_TYPE_HYBRID;
                            break;
                        case GoogleMap.MAP_TYPE_HYBRID:
                            mapType = GoogleMap.MAP_TYPE_NORMAL;
                            break;
                    }
                    map.setMapType(mapType);
                }
                break;

            case R.id.trackMode:
                trackMode = trackMode.next();
                setupMapForTrackMode(trackMode);
                break;

            case R.id.zoomButton:
                zoomToRoute();
                break;

            case R.id.track:
                if (reportedTrack != null) {
                    final CameraPosition old = map.getCameraPosition();
                    CameraPosition position = new CameraPosition.Builder()
                            .bearing(reportedTrack)
                            .target(old.target)
                            .zoom(old.zoom)
                            .build();
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(position);
                    map.animateCamera(cameraUpdate);
                }
                break;

            case R.id.routeClearSelection:
            case R.id.routeClearSelection2:
                clearSelection();
                configureRouteControls(false);
                break;

            case R.id.routeDeleteButton:
                if (selection.isRouteWaypoint()) {
                    Marker selectionMarker = selection.getMarker();
                    fp.deletePoint(selection.getWaypointNumber());
                    clearSelection();
                    setupFreepointSelection(selectionMarker.getPosition(), selectionMarker.getTitle());
                    updateRoute();
                }
                break;

            case R.id.routeAddButton:
                if (!selection.isRouteWaypoint()) {
                    LatLng latLng = selection.getLatLng();

                    Route.RouteWaypoint newWaypoint;
                    if (selection.isFreePoint()) {
                        newWaypoint = Route.RouteWaypoint.make(
                                CoordinateImpl.createCoordinateImpl(selection.getLatLng()), getMagVar(latLng, 0.0f ),
                                selection.getMarker().getTitle(), selection.getMarker().getSnippet());
                    } else {
                        final Waypoint waypoint = selection.getWaypoint();
                        newWaypoint = Route.RouteWaypoint.make(waypoint.getCoord(),
                                waypoint.getMagVar(), waypoint.getIdent(),
                                waypoint.getName());
                    }
                    fp.addPoint(newWaypoint);
                    updateRoute();
                    clearSelection();
                    Selection.legWaypoint(mapRoute, fp.getNumPoints() - 1);
                    configureRouteControls(false);
                }
                break;

            case R.id.routeInsertButton:
                if (!selection.isRouteWaypoint()) {
                    LatLng latLng = selection.getLatLng();

                    Route.RouteWaypoint newWaypoint;
                    if (selection.isFreePoint()) {
                        newWaypoint = Route.RouteWaypoint.make(CoordinateImpl.createCoordinateImpl(latLng),
                                getMagVar(latLng, 0.0f), selection.getMarker().getTitle(), selection.getMarker().getSnippet());
                    } else {
                        final Waypoint waypoint = selection.getWaypoint();
                        newWaypoint = Route.RouteWaypoint.make(waypoint.getCoord(),
                                waypoint.getMagVar(), waypoint.getIdent(),
                                waypoint.getName());
                    }
                    final int optimumInsertPoint = fp.getOptimumInsertPoint(newWaypoint.getCoord());
                    fp.addPoint(newWaypoint, optimumInsertPoint);
                    updateRoute();
                    clearSelection();
                    Selection.legWaypoint(mapRoute, optimumInsertPoint);
                    configureRouteControls(false);
                }
                break;

            case R.id.routeMoveBeforeButton:
                if (selection.isRouteWaypoint() && selection.getWaypointNumber() > 0) {
                    final int waypointNumber = selection.getWaypointNumber();
                    Route.RouteWaypoint toMove = fp.getPoint(waypointNumber);
                    fp.deletePoint(waypointNumber);
                    fp.addPoint(toMove, waypointNumber - 1);
                    updateRoute();
                    clearSelection();
                    Selection.legWaypoint(mapRoute, waypointNumber - 1);
                    configureRouteControls(false);
                }
                break;

            case R.id.routeMoveAfterButton:
                if (selection.isRouteWaypoint() && selection.getWaypointNumber() < fp.getNumPoints() - 1) {
                    final int waypointNumber = selection.getWaypointNumber();
                    Route.RouteWaypoint toMove = fp.getPoint(waypointNumber);
                    fp.deletePoint(waypointNumber);
                    fp.addPoint(toMove, waypointNumber + 1);
                    updateRoute();
                    clearSelection();
                    Selection.legWaypoint(mapRoute, waypointNumber + 1);
                    configureRouteControls(false);
                }
                break;

            default:
                break;
        }
    }

    private float getMagVar(LatLng latLng, float altitude) {
        GeomagneticField gf = new GeomagneticField((float)latLng.latitude, (float)latLng.longitude, altitude, System.currentTimeMillis());
        return (float) Math.toRadians(-gf.getDeclination());
    }

    private void updateRoute() {
        mapRoute.update();
        ai.saveRoute();
        ai.eventBus.post(ai.route);
    }

    /**
     * Configure the map according to the track mode
     */
    private void setupMapForTrackMode(TrackMode mode) {
        if (reportedLocation == null) {
            return;
        }
        if (trackMode != TrackMode.FREE && !locationUpdateLocked) {
            Log.d(LOGTAG, "Updating camera for new location");
            CameraPosition cp = new CameraPosition.Builder()
                    .target(reportedLocation)
                    .zoom(map.getCameraPosition().zoom)
                    .bearing(mode == TrackMode.TRACK ? map.getCameraPosition().bearing : reportedTrack)
                    .build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
            onCameraChange(cp);
        }
        ((Button)findViewById(R.id.trackMode)).setText(getResources().getString(trackMode.getButtonLabelId()));
//        map.getUiSettings().setScrollGesturesEnabled(trackMode == TrackMode.FREE);
    }

    /**
     * Zooms the map so that the current flight mapRoute is within the bounds of the map.
     */
    private void zoomToRoute() {
        if (mapRoute.getRoute().getNumPoints() == 0) {
            return;
        }

        LatLngBounds bounds = mapRoute.getBounds();

        // Extend the bounds to include GPS position
//        if (reportedLocation != null) {
//            bounds = bounds.including(reportedLocation);
//        }

        // Extend the bounds to put a bit of border around the edges (to cope with menus, displays etc).
        // Assumes that the dataItems parent is the top-level container
        int padding = findViewById(R.id.dataItems).getBottom() + getResources().getInteger(R.integer.ZoomRoutePadding);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        map.animateCamera(cameraUpdate);
        trackMode = TrackMode.FREE;
        setupMapForTrackMode(trackMode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.flightMap);
        map = mapFragment.getMap();
        BackgroundGeocoder.initialise(new Geocoder(this.getApplicationContext()));
        DisclaimerDialog disclaimerDialog = new DisclaimerDialog();
        disclaimerDialog.show(getFragmentManager(), "Warnings");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        TextView track = (TextView) findViewById(R.id.track);
        TextView gs = (TextView) findViewById(R.id.speed);
        TextView alt = (TextView) findViewById(R.id.altitude);

        if (location.hasSpeed())
            gs.setText(String.format("%.0f", metresPerSecondToKnots.convert(location.getSpeed())));

        if (location.hasAltitude())
            alt.setText(String.format("%.0f", metresToFeet.convert(location.getAltitude())));

        aircraft.setVisible(true);
        if (location.hasBearing()) {
            GeomagneticField mf = new GeomagneticField((float) location.getLatitude(), (float) location.getLongitude(),
                    0.0f, System.currentTimeMillis());
            reportedTrack = addDegrees(location.getBearing(), -mf.getDeclination());
            track.setText(String.format("%03.0f", reportedTrack));
        }
        reportedLocation = new LatLng(location.getLatitude(), location.getLongitude());
        ai.eventBus.post(location);
        aircraft.setRotation(reportedTrack);
        aircraft.setPosition(reportedLocation);
        setupMapForTrackMode(trackMode);
    }

    private float addDegrees(float deg1, float deg2) {
        final float d = deg1 + deg2;
        if (d < 0.0) return d + 360.0f;
            else if (d > 359.99) return d - 360f;
            else return d;
    }

    @Override
    public void onProviderDisabled(String provider) {
        disableLocation();
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (status != LocationProvider.AVAILABLE) {
            disableLocation();
        }
    }

    private void disableLocation() {
        for (int id : new int[]{R.id.track, R.id.speed, R.id.altitude}) {
            TextView view = (TextView) findViewById(id);
            view.setText(R.string.dashes);
        }
    }

    /**
     *
     */
    private void setupMapFromResume() {

        if (map == null) {
            return;
        }

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setOnMapLongClickListener(this);
        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);
        map.setOnInfoWindowClickListener(this);
        map.setOnMapLoadedCallback(this);
        map.setOnCameraChangeListener(this);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setCompassEnabled(true);

        final View db = findViewById(R.id.dataBlock);
        db.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                map.setPadding(0, db.getBottom(),0,0);
            }
        });
        map.clear();
        mapRoute = new MapRoute(map, ai.route);
        map.setOnMarkerDragListener(this);
        setupAircraft();

        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        float lat = prefs.getFloat("lat", 51.6f);
        float lon = prefs.getFloat("lon", -2.0f);
        LatLng latLng = new LatLng(lat, lon);
        float zoom = prefs.getFloat("zoom", 6.0f);
        float bearing = prefs.getFloat("bearing", 0.0f);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(latLng, zoom, 0.0f, bearing));
        map.animateCamera(cameraUpdate);

    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        dragHandler = new RouteDragHandler(mapRoute, selection, updateInfoWindowRunnable);
        dragHandler.start(marker);
        if (menuState) toggleMenu(false);

        final int wpIndex = mapRoute.getMarkerIndex(marker);
        if (wpIndex != -1) {
            Selection.legWaypoint(mapRoute, wpIndex);
        }

        animateView(findViewById(R.id.dataBlock), R.anim.out_top, View.INVISIBLE);
        configureRouteControls(true);
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        dragHandler.drag(marker);
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        dragHandler.end(marker);
        if (menuState) toggleMenu(true);
        animateView(findViewById(R.id.dataBlock), R.anim.in_from_top, View.VISIBLE);
        configureRouteControls(false);
        //TODO: Workaround to posting event, which causes an unecessary update to the mapRoute and loses info window
//        ai.eventBus.post(ai.route);
        planWindowController.updateRoute(ai.route);
    }

    private void setupAircraft() {
        // aircraft will be drawn during position updates
        // Initialise the map position if we can.
        final Location lastKnownLocation = locationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        LatLng acPosition;
        if (lastKnownLocation != null) {
            acPosition = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        } else {
            acPosition = ukCentre;
        }

        aircraft = map.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.aircraft))
                .anchor(0.5f, 0.5f)
                .position(acPosition)
                .flat(true)
                .title(aircraftMarkerTitle)
                .visible(false));
    }

    @Subscribe
    public void updatedWaypointsDB(WaypointDB wpdb) {
        Log.d(LOGTAG, "Wpdb updated, has " + ai.wpdb.getWaypoints().size() + " entries");
        setupWaypoints();
    }

    private void setupWaypoints() {
        Log.d(LOGTAG, "Setup waypoints");
        if (waypointElements == null) {
            waypointElements = new MapElements<Waypoint, MapWaypoint>();
        }

        final float zoom = map.getCameraPosition().zoom;
        if (zoom < ZOOM_WAYPOINT_CUTOFF) {
            for (Waypoint w : waypointElements.getAll()) {
                waypointElements.get(w).remove();
            }
            waypointElements.clear();
            Log.d(LOGTAG, "Cutoff - bailing");
            return;
        }

        // Compute the bounds of the current map
        Bounds.Builder builder = new Bounds.Builder();
        final LatLngBounds latLngBounds = map.getProjection().getVisibleRegion().latLngBounds;
        builder.including(CoordinateImpl.createCoordinateImpl(latLngBounds.southwest));
        builder.including(CoordinateImpl.create(latLngBounds.northeast.latitude + 0.2, latLngBounds.northeast.longitude));


        // Work out the updates to apply to the map, then immediately remove the unwanted elements
        MapElements.Update<Waypoint> update = waypointElements.getUpdateRecord(ai.wpdb.getWithinBounds(builder.build(), WAYPOINT_LIMIT));
        for (Waypoint w : update.getToDelete()) {
            waypointElements.get(w).remove();
            waypointElements.remove(w);
        }

        BitmapDescriptor airfieldBitmap = BitmapDescriptorFactory.fromResource(R.drawable.airfield);
        BitmapDescriptor vorBitmap = BitmapDescriptorFactory.fromResource(R.drawable.vordme);

        for (Waypoint w : update.getToAdd()) {
            LatLng point = asLatLng(w.getCoord());
            BitmapDescriptor bm = null;
            if (w.getInfo().getType() == InfoMap._AIRPORT) {
                bm = airfieldBitmap;
            } else if (w.getInfo().getType() == InfoMap._VORDME) {
                bm = vorBitmap;
            }
            if (bm != null) {
                MarkerOptions mo = new MarkerOptions().position(point)
                        .title(w.getIdent())
                        .snippet(w.getName())
                        .anchor(0.5f, 0.5f)
                        .icon(bm);
                Marker m = map.addMarker(mo);
                GroundOverlay overlay = null;
                if (zoom > ZOOM_OVERLAY_CUTOFF) {
                    if (bm == vorBitmap) {
                        BitmapDescriptor compass = BitmapDescriptorFactory.fromResource(R.drawable.vorcompass);
                        GroundOverlayOptions newOverlay = new GroundOverlayOptions()
                                .image(compass)
                                .position(point, VOR_RADIUS * 2)
                                .transparency(0.0f)
                                .bearing(w.getMagVar());
                        overlay = map.addGroundOverlay(newOverlay);
                    } else {
                        BitmapDescriptor atz = BitmapDescriptorFactory.fromResource(R.drawable.atz);
                        GroundOverlayOptions newOverlay = new GroundOverlayOptions()
                                .image(atz)
                                .position(point, ATZ_RADIUS * 2)
                                .transparency(0.5f);
                        overlay = map.addGroundOverlay(newOverlay);
                    }
                }
                waypointElements.add(w, new MapWaypoint(m, overlay));
            }
        }
        Log.d(LOGTAG, "Setup waypoints - fin : " + waypointElements.size());
    }

    @Subscribe
    public void updatedAirspaceDB(AirspaceDB asdb) {
        Log.d(LOGTAG, "Airspace db updated");
        setupAirspace();
    }

    private void setupAirspace() {
        Log.d(LOGTAG, "Setup airspace");
        if (airspaceElements == null) {
            airspaceElements = new MapElements<Airspace, MapAirspace>();
        }

        if (airspaceQueue == null) {
            airspaceQueue = new LinkedBlockingQueue<Airspace>();
        } else {
            // Flush current work queue
            airspaceQueue.clear();
        }

        // Only draw airspace within a certain zoom level
        Log.d(LOGTAG, "Zoom:" + map.getCameraPosition().zoom);
        if (map.getCameraPosition().zoom < ZOOM_AIRSPACE_CUTOFF) {
//            if (airspaceTask != null) {
//                Log.d(LOGTAG, "Cancelling airspace task");
//                airspaceTask.cancel(true);
//            }
            if (airspaceQueue.size() > 0) {
                airspaceQueue.clear();
            }
            for (Airspace a : airspaceElements.getAll()) {
                airspaceElements.get(a).remove();
            }
            airspaceElements.clear();
            return;
        }

        // TODO: Refactor with setupWaypoints
        // Compute the bounds of the current map
        Bounds.Builder builder = new Bounds.Builder();
        final LatLngBounds latLngBounds = map.getProjection().getVisibleRegion().latLngBounds;
        builder.including(CoordinateImpl.createCoordinateImpl(latLngBounds.southwest));
        builder.including(CoordinateImpl.create(latLngBounds.northeast.latitude + 0.2, latLngBounds.northeast.longitude));

        // Work out the updates to apply to the map, then immediately remove the unwanted elements
        final Collection<Airspace> withinBounds = ai.asdb.getWithinBounds(builder.build(), AIRSPACE_LIMIT);
        boolean possibleClipping = (withinBounds.size() == AIRSPACE_LIMIT);

        // TODO: ** Refactor toast control to own class
        if (possibleClipping && clipToastTimer == null) {
            clipToast = Toast.makeText(getApplicationContext(), "Map may be clipped!", Toast.LENGTH_LONG);
            clipToast.show();
            clipToastTimer = new CountDownTimer(7000, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    clipToast.show();
                }

                @Override
                public void onFinish() {
                    Log.d(LOGTAG, "Cancelling clipToast timer");
                    clipToast.cancel();
                    clipToastTimer = null;
                }
            };
            clipToastTimer.start();
        } else if (!possibleClipping) {
            if (clipToastTimer != null) {
                clipToast.cancel();
                clipToastTimer.cancel();
                clipToastTimer = null;
            }
        }

        MapElements.Update<Airspace> update = airspaceElements.getUpdateRecord(withinBounds);
        for (Airspace a : update.getToDelete()) {
            // TODO: call remove from the Elements class?
            airspaceElements.get(a).remove();
            airspaceElements.remove(a);
        }

        // Queue the airspaces to add so that the background task picks them up
        airspaceQueue.addAll(update.getToAdd());
        if (airspaceTask == null && airspaceQueue.size() > 0) {
            Log.d(LOGTAG, "Launching new airspaceTask");
            airspaceTask = new AirspaceTask(map, airspaceQueue, airspaceElements, new AirspaceTask.OnCompleted() {
                @Override
                public void onCompleted() {
                    Log.d(LOGTAG, "Airspace task reports completed");
                    airspaceTask = null;
                }
            });
            airspaceTask.execute();
        }
        Log.d(LOGTAG, "Setup airspace - fin : " + airspaceElements.size());
    }


    @Override
    protected void onPause() {
        Log.d(LOGTAG, "Pausing");
        super.onPause();
        if (airspaceTask != null) {
            airspaceTask.cancel(true);
        }
        airspaceTask = null;
        airspaceElements = null;
        airspaceQueue = null;
        locationMgr.removeUpdates(this);

        CameraPosition cp = map.getCameraPosition();
        SharedPreferences.Editor prefs = getPreferences(MODE_PRIVATE).edit();
        prefs.putFloat("lat", (float)cp.target.latitude);
        prefs.putFloat("lon", (float) cp.target.longitude);
        prefs.putFloat("zoom", cp.zoom);
        prefs.putFloat("bearing", cp.bearing);
        prefs.commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOGTAG, "Stopping");
    }

    @Override
    protected void onResume() {
        // TODO: **  Refactor to use onClick attribute in XML for all buttons.
        Log.d(LOGTAG, "Resume");
        com.gpsaviator.android.Utils.setClickHandler(this, this, this,
                new int[]{R.id.satViewToggle, R.id.trackMode, R.id.zoomButton, R.id.track,
                        R.id.routeClearSelection, R.id.routeAddButton, R.id.routeInsertButton, R.id.routeDeleteButton,
                        R.id.routeMoveBeforeButton, R.id.routeMoveAfterButton, R.id.routeClearSelection2,
                        R.id.menuButton, R.id.clearRoute, R.id.undoClear});

        ai = ((AviatorApp) getApplication()).i();
        ai.eventBus.register(this);

        locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        View planWindowView = findViewById(R.id.planWindowView);
        planWindowController = new PlanList(planWindowView, ai.route, ai.eventBus);

        planWindowView.setVisibility(View.INVISIBLE);
        (findViewById(R.id.mainMenu)).setVisibility(View.INVISIBLE);

        setupMapFromResume();
        configureRouteControls(false);
        super.onResume();
    }

    private final Runnable cameraUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastCameraUpdateTime > 0) {
                Log.d(LOGTAG, "Updating map for camera change");
                setupAirspace();
                setupWaypoints();
                lastCameraUpdateTime = currentTime;
            }
        }
    };

    private boolean locationUpdateLocked = false;
    private final Runnable cameraUpdateForTrackRunnable = new Runnable() {
        @Override
        public void run() {
            locationUpdateLocked = false;
            setupMapForTrackMode(trackMode);
        }
    };

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        Log.d(LOGTAG, "Camera: " + cameraPosition.zoom);
        if (map == null) {
            return;
        }
        Log.d(LOGTAG, "Camera update");
        if (airspaceQueue != null) airspaceQueue.clear();
        handler.removeCallbacks(cameraUpdateRunnable);
        handler.postDelayed(cameraUpdateRunnable, 500);

        locationUpdateLocked = true;
        handler.removeCallbacks(cameraUpdateForTrackRunnable);
        handler.postDelayed(cameraUpdateForTrackRunnable, 5000);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Log.d(LOGTAG, "Map long click");
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(LOGTAG, "Marker click");

        if (marker.getTitle().equals(aircraftMarkerTitle)) {
            onMapClick(marker.getPosition());
            return true;
        }

        if (selection.isSelected() && marker != selection.getMarker()) {
            Log.d(LOGTAG, "Clicked marker is not selection");
        }

        // ignore clicks on the current selected marker
        if (selection.isSelected() && selection.getMarker().getId().equals(marker.getId())) {
            Log.d(LOGTAG, "Clicked marker has same Id, ignoring click");
            marker.showInfoWindow();
            return true;
        }
        clearSelection();

        final int wpIndex = mapRoute.getMarkerIndex(marker);
        if (wpIndex != -1) {
            Selection.legWaypoint(mapRoute, wpIndex);
        } else {
            Waypoint newWaypoint = new Waypoint(CoordinateImpl.createCoordinateImpl(marker.getPosition()),
                    getMagVar(marker.getPosition(), 0.0f), 0.0f, marker.getTitle(), marker.getSnippet());
            Selection.waypoint(newWaypoint, marker);
        }
        marker.showInfoWindow();
        configureRouteControls(false);
        return true;
    }


    private void clearSelection() {
        if (selection.isSelected()) {
            selection.clear();
        }
        BackgroundGeocoder.getInstance().stop();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(LOGTAG, "Map clicked");
        clearSelection();
        BackgroundGeocoder.getInstance().start(latLng, "Wp", updateInfoWindowRunnable);
        setupFreepointSelection(latLng, "Wp");
    }

    private void setupFreepointSelection(LatLng latLng, String title) {
        MarkerOptions newMarker = new MarkerOptions()
                .draggable(false)
                .position(latLng);
        if (title != null) {
            newMarker = newMarker.title(title);
        }
        Marker marker = map.addMarker(newMarker);
        Selection.freePoint(marker);
        configureRouteControls(false);
    }

    private void animateView(View view, int anim_resource, int visibility) {
        if (view.getVisibility() == visibility) {
            return;
        }
        Animation animation = AnimationUtils.loadAnimation(this, anim_resource);
        view.startAnimation(animation);
        view.setVisibility(visibility);

    }
    // TODO: Wrap into own class, and call from drag handler to show CLR button
    private void configureRouteControls(boolean override) {
        final LinearLayout addInsertControls = (LinearLayout) findViewById(R.id.routeControls1);
        final LinearLayout moveControls = (LinearLayout) findViewById(R.id.routeControls2);

        if (override) {
            animateView(addInsertControls, R.anim.out_right, View.INVISIBLE);
            animateView(moveControls, R.anim.out_right, View.INVISIBLE);
            return;
        }

        if (!selection.isSelected()) {
            addInsertControls.setVisibility(View.INVISIBLE);
            moveControls.setVisibility(View.INVISIBLE);
            return;
        }

        LinearLayout controlsToShow;
        LinearLayout controlsToHide;
        if (selection.isRouteWaypoint()) {
            controlsToShow = moveControls;
            controlsToHide = addInsertControls;
        } else {
            controlsToShow = addInsertControls;
            controlsToHide = moveControls;
            final Button insertButton = (Button) findViewById(R.id.routeInsertButton);
            if (mapRoute.getRoute().getNumPoints() < 2) {
                insertButton.setVisibility(View.INVISIBLE);
            } else {
                insertButton.setVisibility(View.VISIBLE);
            }
        }

        controlsToHide.clearAnimation();
        controlsToHide.setVisibility(View.INVISIBLE);
        animateView(controlsToShow, R.anim.in_from_right, View.VISIBLE);
    }

    @Override
    public void onMapLoaded() {
    }

    @Subscribe
    public void updateRoute(Route newRoute) {
        mapRoute.update();
    }
}