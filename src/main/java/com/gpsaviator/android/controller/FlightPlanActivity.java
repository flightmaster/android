package com.gpsaviator.android.controller;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.gpsaviator.*;
import com.gpsaviator.Route.Leg;
import com.gpsaviator.android.AviatorApp;
import com.gpsaviator.android.model.LegListAdapter;
import com.gpsaviator.android.R;

public class FlightPlanActivity extends Activity implements OnClickListener {

	private final int ADD_AFTER = RESULT_FIRST_USER + 1;
	private final int ADD_LAST = RESULT_FIRST_USER + 2;
	private CoreApplication ai;
	private final int CHANGE_FIRST = RESULT_FIRST_USER + 3;
	private final int CHANGE_SECOND = RESULT_FIRST_USER + 4;
	private final int DELETE_FIRST = RESULT_FIRST_USER + 5;
	private final int DELETE_SECOND = RESULT_FIRST_USER + 6;
	private final int INSERT_BEFORE_FIRST = RESULT_FIRST_USER + 7;
	private final int INSERT_BEFORE_SECOND = RESULT_FIRST_USER + 8;

	private int selectedListItem;

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.newWaypointButton:
			callSelectWaypoint(0, ADD_LAST);
			break;

		default:
			break;
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item.getMenuInfo();

		switch (item.getItemId()) {
		case R.id.deleteFirstWaypoint:
			ai.route.deletePoint(acmi.position);
			setupPlanList();
			break;

		case R.id.deleteSecondWaypoint:
			ai.route.deletePoint(acmi.position + 1);
			setupPlanList();
			break;

		case R.id.insertBeforeFirstWaypoint:
			callSelectWaypoint(acmi.position, INSERT_BEFORE_FIRST);
			break;

		case R.id.insertBeforeSecondWaypoint:
			callSelectWaypoint(acmi.position, INSERT_BEFORE_SECOND);
			break;

		case R.id.addAfterSecondWaypoint:
			callSelectWaypoint(acmi.position, ADD_AFTER);
			break;

		case R.id.changeFirstWaypoint:
			callSelectWaypoint(acmi.position, CHANGE_FIRST);
			break;

		case R.id.changeSecondWaypoint:
			callSelectWaypoint(acmi.position, CHANGE_SECOND);
			break;

		default:
			return super.onContextItemSelected(item);
		}
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flight_plan);
		ai = ((AviatorApp) getApplication()).i();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_flightplan, menu);

		AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) menuInfo;

		/*
		 * deactivate the deleteSecond option if we're dealing with the case of
		 * a single waypoint in a flightplan. NB don't need to handle case of
		 * numTurnPoints == 0 because the context menu can't be displayed when
		 * the listview is empty!
		 */

		menu.removeItem(R.id.addAfterSecondWaypoint); // removed for screen
														// space, for now

		if (ai.route.getNumPoints() == 1) {
			menu.removeItem(R.id.insertBeforeSecondWaypoint);
			menu.removeItem(R.id.deleteSecondWaypoint);
			menu.removeItem(R.id.changeSecondWaypoint);

			Route.RouteWaypoint wp = ai.route.getPoint(acmi.position);
			setupContextOption(menu, R.id.insertBeforeFirstWaypoint, wp);
			setupContextOption(menu, R.id.deleteFirstWaypoint, wp);
			setupContextOption(menu, R.id.changeFirstWaypoint, wp);

		} else {
			Leg leg = ai.route.getLeg(acmi.position);
			setupContextOption(menu, R.id.insertBeforeFirstWaypoint, leg.getStart());
			setupContextOption(menu, R.id.insertBeforeSecondWaypoint, leg.getEnd());
			// setupContextOption(menu, R.id.addAfterSecondWaypoint,
			// leg.second);
			setupContextOption(menu, R.id.deleteFirstWaypoint, leg.getStart());
			setupContextOption(menu, R.id.deleteSecondWaypoint, leg.getEnd());
			setupContextOption(menu, R.id.changeFirstWaypoint, leg.getStart());
			setupContextOption(menu, R.id.changeSecondWaypoint, leg.getEnd());
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_flight_plan, menu);
		return true;

	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.newFlightPlan:
			ai.route.create();
			setupPlanList();
			break;

		case R.id.showMap:
			Intent i = new Intent(this, MainActivity.class);
			startActivity(i);
			break;

		default:
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void callSelectWaypoint(int position, int command) {
		selectedListItem = position;
		Intent intent = new Intent(this, SelectWaypointActivity.class);
		startActivityForResult(intent, command);
	}

	private void insertWaypoints(int[] waypointIDs, int insertPosition) {
		for (int j = 0; j < waypointIDs.length; j++) {
			Waypoint wp = ai.wpdb.getWaypoint(waypointIDs[j]);
			ai.route.addPoint(Route.RouteWaypoint.make(wp.getCoord(), wp.getMagVar(), wp.getIdent(), wp.getName()),
					   		  insertPosition + j);
		}
	}

	private void setupContextOption(ContextMenu menu, int menuItemID, Route.RouteWaypoint wp) {

		MenuItem item = menu.findItem(menuItemID);
		String deleteStr = String.format(item.getTitle().toString(), wp.getIdent());
		item.setTitle(deleteStr);

	}

	private ListView setupPlanList() {
		ListView planList = (ListView) findViewById(R.id.planWaypointList);

		Log.d("COUNT", Integer.toString(ai.route.getNumPoints()));
		if (ai.route.getNumLegs() > 0) {
			ArrayList<Leg> list = ai.route.getLegs();
//			LegListAdapter adapter = new LegListAdapter(this, R.layout.leg_item, list, false, 0.0);
//			planList.setAdapter(adapter);
//			adapter.notifyDataSetChanged();
			Log.d("SETUP", "legs");
		} else {
			List<Route.RouteWaypoint> list = ai.route.getPoints();
			ArrayAdapter<Route.RouteWaypoint> adapter = new ArrayAdapter<Route.RouteWaypoint>(this, R.layout.simplerow, list);
			planList.setAdapter(adapter);
			adapter.notifyDataSetChanged();
			Log.d("SETUP", "waypoint");
		}

		setupSummaryRow();

		return planList;
	}

	private void setupSummaryRow() {
		LinearLayout row = (LinearLayout) findViewById(R.id.summaryRow);
		int visible;

		Route fp = ai.route;
		if (fp.getNumLegs() > 1) {
			double totalDistance = 0.0;
			for (int leg = 0; leg < fp.getNumLegs(); leg++) {
				totalDistance += fp.getLeg(leg).getRange();
			}
			TextView distance = (TextView) findViewById(R.id.totalDistance);
			distance.setText(String.format("%.1fnm", DMS.rad2nm(totalDistance)));

			double bearing = fp.getPoints().get(0).getCoord()
					.bearingTo(fp.getPoints().get(fp.getNumPoints() - 1).getCoord());

			TextView bearingText = (TextView) findViewById(R.id.totalTrack);
			bearingText.setText(String.format("%03.0f%c", Math.toDegrees(bearing), (char) 0x00B0));
			visible = View.VISIBLE;
		} else {
			visible = View.GONE;
		}
		row.setVisibility(visible);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		Log.d("REQ", Integer.toString(requestCode));
		Log.d("RESULT", Integer.toString(resultCode));

		if (resultCode != RESULT_OK) {
			return;
		}

		int waypointIDs[] = data.getExtras().getIntArray(SelectWaypointActivity.SELECTED_WAYPOINT);
		switch (requestCode) {
		case ADD_LAST:
			insertWaypoints(waypointIDs, ai.route.getPoints().size());
			break;

		case INSERT_BEFORE_FIRST:
			insertWaypoints(waypointIDs, selectedListItem);
			break;

		case INSERT_BEFORE_SECOND:
			insertWaypoints(waypointIDs, selectedListItem + 1);
			break;

		case ADD_AFTER:
			insertWaypoints(waypointIDs, selectedListItem + 2);
			break;

		case DELETE_FIRST:
			ai.route.deletePoint(selectedListItem);
			break;

		case DELETE_SECOND:
			ai.route.deletePoint(selectedListItem + 1);
			break;

		case CHANGE_FIRST:
			ai.route.deletePoint(selectedListItem);
			insertWaypoints(waypointIDs, selectedListItem);
			break;

		case CHANGE_SECOND:
			ai.route.deletePoint(selectedListItem + 1);
			insertWaypoints(waypointIDs, selectedListItem + 1);
			break;

		default:
			break;

		}
		ai.saveRoute();

	}

	@Override
	protected void onResume() {
		super.onResume();

		Button addButton = (Button) findViewById(R.id.newWaypointButton);
		addButton.setOnClickListener(this);

		ListView planList = setupPlanList();
		registerForContextMenu(planList);

	}
}
