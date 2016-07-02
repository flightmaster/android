package com.gpsaviator.android.controller;

import android.app.Activity;
import com.google.android.gms.maps.MapFragment;
import com.gpsaviator.CoordinateFactory;
import com.gpsaviator.android.model.CoordinateImplFactory;
import com.gpsaviator.android.MultiToggleButton;
import com.gpsaviator.android.R;
import com.gpsaviator.encoders.json.WaypointJsoniser;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.hardware.GeomagneticField;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.gpsaviator.Coordinate;
import com.gpsaviator.DMS;
import com.gpsaviator.Waypoint;

public class EditWaypointActivity extends Activity implements OnClickListener,
		OnMarkerDragListener {

	public class AxisManager implements TextWatcher, OnClickListener {

		private final EditText degs;
		private CountDownTimer editTimer = null;
		private final TextView label;
		private final EditText mins;
		private final MultiToggleButton nsew;
		private final EditText secs;

		private final AxisType type;
		private Boolean valid;
		private double value;
		private final int nextInputFocusField;

		AxisManager(AxisType axisType, double value, int degs, int mins, int secs,
				int latitudelabel, int nsew, int nextInputFocusField) {
			type = axisType;
			this.degs = (EditText) findViewById(degs);
			this.mins = (EditText) findViewById(mins);
			this.secs = (EditText) findViewById(secs);
			label = (TextView) findViewById(latitudelabel);
			this.nsew = (MultiToggleButton) findViewById(nsew);
			if (type == AxisType.LAT_AXIS) {
				this.nsew.setLabels(new String[] { "N", "S" });
			} else {
				this.nsew.setLabels(new String[] { "E", "W" });
			}

			// NB Call before adding textChangedListeners, otherwise get a
			// problem
			setValue(value, true);

			this.degs.addTextChangedListener(this);
			this.mins.addTextChangedListener(this);
			this.secs.addTextChangedListener(this);

			this.nsew.setOnClickListener(this);

			this.nextInputFocusField = nextInputFocusField;

		}

		@Override
		public void afterTextChanged(Editable e) {

			Pair<Boolean, Float> valid = rangeCheck();
			if (valid.first) {
				label.setError(null);
				value = valid.second;
				resetTimer();
			} else {
				label.setError("Invalid");
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		public double getValue() {
			return value;
		}

		public Boolean isValid() {
			return valid;
		}

		@Override
		public void onClick(View v) {
			setValue(-value, false);
			updatedAxis();
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		private double getMagnitude() {
			return nsew.getValue() == 0 ? 1.0 : -1.0;
		}

		private Pair<Boolean, Float> rangeCheck() {

			if (degs.length() == 0 || mins.length() == 0 || secs.length() == 0) {
				return new Pair<Boolean, Float>(false, 0.0f);
			}

			boolean decimalDegs = degs.getText().toString().contains(".");
			boolean decimalMins = decimalDegs ? false : mins.getText().toString().contains(".");

			int minsVisible;
			int secsVisible;
			int degsNextFocus = mins.getId();
			int minsNextFocus = secs.getId();
			if (decimalDegs) {
				minsVisible = View.GONE;
				secsVisible = View.GONE;
				degsNextFocus = nextInputFocusField;
			} else {
				if (decimalMins) {
					minsVisible = View.VISIBLE;
					secsVisible = View.GONE;
					minsNextFocus = nextInputFocusField;
				} else {
					minsVisible = View.VISIBLE;
					secsVisible = View.VISIBLE;
				}
			}
			degs.setNextFocusDownId(degsNextFocus);
			mins.setNextFocusDownId(minsNextFocus);
			mins.setVisibility(minsVisible);
			secs.setVisibility(secsVisible);

			// mins.setNextFocusDownId(minsNextFocus);

			double d = Double.valueOf(degs.getText().toString());
			double m = decimalDegs ? 0 : Double.valueOf(mins.getText().toString());
			double s = decimalDegs || decimalMins ? 0 : Double.valueOf(secs.getText().toString());

			if (m < 0 || m >= 60 || s < 0 || s >= 60) {
				return new Pair<Boolean, Float>(false, 0.0f);
			}

			double newValue = d + m / 60 + s / 3600;

			double lower = 0, upper = 0;

			upper = type == AxisType.LAT_AXIS ? 90 : 180;

			if (newValue < lower || newValue > upper) {
				return new Pair<Boolean, Float>(false, 0.0f);
			} else {
				return new Pair<Boolean, Float>(true,
						(float) Math.toRadians(newValue * getMagnitude()));
			}

		}

		private void resetTimer() {
			if (editTimer != null) {
				editTimer.cancel();
			}
			editTimer = new CountDownTimer(2000, 2000) {

				@Override
				public void onFinish() {
					updatedAxis();
				}

				@Override
				public void onTick(long millisUntilFinished) {

				}
			};
			editTimer.start();
		}

		private void setValue(double value, boolean updateDMS) {

			if (updateDMS) {
				String arr[] = (new DMS(value)).toStringArray();

				degs.setText(type == AxisType.LAT_AXIS ? arr[0].subSequence(1, 3) : arr[0]);
				mins.setText(arr[1]);
				secs.setText(arr[2]);
			}
			nsew.setValue(value < 0 ? 1 : 0);
			this.value = value;
		}

	}

	public enum AxisType {
		LAT_AXIS, LON_AXIS
	}

	public static final String WAYPOINT_BUNDLE = "WAYPOINT";

	public static final int WAYPOINT_CHANGED = RESULT_FIRST_USER + 2;
	private static final int WAYPOINT_UNCHANGED = RESULT_FIRST_USER + 3;

	private Waypoint inputWaypoint;

	// TODO: Axis manager shouldn't be updating the map directly
	// TODO: Use single external countdowntimer

	private AxisManager latAxisMan, lonAxisMan;

	private GoogleMap map = null;
	private Marker marker = null;

	double log2(double num) {
		return (Math.log(num) / Math.log(2));
	}

	private CoordinateFactory cf = CoordinateImplFactory.getInstance();
	private WaypointJsoniser wj = new WaypointJsoniser(cf);

	@Override
	public void onBackPressed() {
		Coordinate coord = cf.create(Math.toDegrees(latAxisMan.getValue()), Math.toDegrees(lonAxisMan.getValue()));
		String ident = ((TextView) findViewById(R.id.ident)).getText().toString();
		String name = ((TextView) findViewById(R.id.name)).getText().toString();
		float magVar = Float.valueOf(((TextView) findViewById(R.id.magVarn)).getText().toString());
		Button mvButton = (Button) findViewById(R.id.magVarnButton);

		if (mvButton.getText().toString().compareTo("W") == 0) {
			magVar = -magVar;
		}
		Waypoint newwp = new Waypoint(coord, (float) Math.toRadians(magVar), 0.0f, ident, name);
		newwp.setInfo(inputWaypoint.getInfo());

		try {
			JSONObject jsonObj;
			jsonObj = wj.toJson(newwp);
			String oldWaypointString = wj.toJson(inputWaypoint).toString();
			String newWaypointString = jsonObj.toString();
			Log.d("JSON", oldWaypointString);
			Log.d("JSON", newWaypointString);

			Intent i = new Intent();
			i.putExtra(WAYPOINT_BUNDLE, jsonObj.toString());
			if (oldWaypointString.compareTo(newWaypointString) != 0) {
				// Pass the waypoint data back to the caller in JSON format
				setResult(WAYPOINT_CHANGED, i);
				finish();
			} else {
				setResult(WAYPOINT_UNCHANGED, i);
				finish();
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.magVarnButton:
			Button b = (Button) v;

			if (b.getText().toString().compareTo("E") == 0) {
				b.setText("W");
			} else {
				b.setText("E");
			}
			break;

		case R.id.calcMagVar:
			float lat = (float) Math.toDegrees(latAxisMan.getValue());
			float lon = (float) Math.toDegrees(lonAxisMan.getValue());
			float alt = 0.0f;
			long time = System.currentTimeMillis();

			GeomagneticField mf = new GeomagneticField(lat, lon, alt, time);
			setupMagVar((float) Math.toRadians(mf.getDeclination()));
			break;

		default:
			break;
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// SupportMapFragment fragment = new SupportMapFragment();
		// getSupportFragmentManager().beginTransaction().add(android.R.id.content,
		// fragment).commit();

		setContentView(R.layout.activity_edit_waypoint);

		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.editMap))
				.getMap();

		if (map != null) {
			map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
			map.setOnMarkerDragListener(this);
		}

		Waypoint wp = null;

		try {
			String newWP = getIntent().getExtras().getString(WAYPOINT_BUNDLE);
			Log.d("JSON-IN", newWP);
//			wp = new Waypoint(newWP);
//			inputWaypoint = wp;

		} catch (Exception e) {
			wp = new Waypoint(cf.create(41,-2), -2.0f, 0.0f, "NONE", "JSON Exception");
		}

		latAxisMan = new AxisManager(AxisType.LAT_AXIS, wp.getCoord().getLat(), R.id.latDegs,
				R.id.latMins, R.id.latSecs, R.id.latitudeLabel, R.id.northSouth, R.id.lonDegs);

		lonAxisMan = new AxisManager(AxisType.LON_AXIS, wp.getCoord().getLon(), R.id.lonDegs,
				R.id.lonMins, R.id.lonSecs, R.id.longitudeLabel, R.id.eastWest, R.id.magVarn);

		TextView id = (TextView) findViewById(R.id.ident);
		TextView name = (TextView) findViewById(R.id.name);

		id.setText(wp.getIdent());
		name.setText(wp.getName());

		setupMagVar(wp.getMagVar());
		updatedAxis();
	}

	@Override
	public void onMarkerDrag(Marker marker) {

		LatLng latLon = marker.getPosition();

		latAxisMan.setValue(Math.toRadians(latLon.latitude), true);
		lonAxisMan.setValue(Math.toRadians(latLon.longitude), true);

	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		onMarkerDrag(marker);
		updatedAxis();
	}

	@Override
	public void onMarkerDragStart(Marker arg0) {
		// TODO Auto-generated method stub

	}

	private void setupMagVar(float mv) {
		MultiToggleButton mvToggle = (MultiToggleButton) findViewById(R.id.magVarnButton);
		TextView magVar = (TextView) findViewById(R.id.magVarn);

		magVar.setText(String.format("%.1f", (float) Math.abs(Math.toDegrees(mv))));
		mvToggle.setLabels(new String[] { "E", "W" });
		mvToggle.setValue(mv < 0.0f ? 1 : 0);
		mvToggle.setOnClickListener(this);

		((Button) findViewById(R.id.calcMagVar)).setOnClickListener(this);
	}

	private void updatedAxis() {

		double lat = Math.toDegrees(latAxisMan.getValue());
		double lon = Math.toDegrees(lonAxisMan.getValue());

		if (map != null) {
			float density = getResources().getDisplayMetrics().density;
			float widthDP = (getResources().getDisplayMetrics().widthPixels) / density;

			LatLng latLng = new LatLng(lat, lon);
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, (float) log2(4320)));

			String ident = ((EditText) findViewById(R.id.ident)).getText().toString();
			if (marker != null) {
				marker.remove();
			}
			marker = map.addMarker(new MarkerOptions().position(latLng).title(ident)
					.draggable(true));
		}
	}
	// @Override
	// protected boolean isRouteDisplayed() {
	// return false;
	// }

}
