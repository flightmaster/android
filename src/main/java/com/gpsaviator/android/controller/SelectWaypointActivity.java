package com.gpsaviator.android.controller;

import java.util.ArrayList;

import com.gpsaviator.CoordinateFactory;
import com.gpsaviator.android.AviatorApp;
import com.gpsaviator.android.model.CoordinateImplFactory;
import com.gpsaviator.android.R;
import com.gpsaviator.encoders.json.WaypointJsoniser;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.gpsaviator.CoreApplication;
import com.gpsaviator.Waypoint;

public class SelectWaypointActivity extends SavedActivity implements OnClickListener, OnItemClickListener,
		OnEditorActionListener, TextWatcher {

	public final static int SELECT_WAYPOINT = 0;

	private static final int FORCE_WAYPOINT_CHANGE = 1;

	static final String SELECTED_WAYPOINT = "SELECTED_WAYPOINT";

	private CoreApplication ai;

	// TODO: Custom adapter as at URL:
	// http://www.learn-android.com/2011/11/22/lots-of-lists-custom-adapter/3/

	private ArrayAdapter<Waypoint> listAdapter = null;

	private int listScrollTo;

	private boolean displayByNames = false;

    SelectWaypointActivity() {
        super();
    }

	@Override
	public void afterTextChanged(Editable s) {

		String str = s.toString();

		/*
		 * if there's a space in the input string then the user is specifying
		 * multiple idents.
		 */

		if (str.endsWith(" ")) {
			validateWaypointInput();
		}
		scrollToUserInput(str);

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		// Intentionally blank
	}

	// TODO: Move to application-level
	private CoordinateFactory cf = CoordinateImplFactory.getInstance();
	private WaypointJsoniser wj = new WaypointJsoniser(cf);

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.newWaypointButton:
			Waypoint newwp = new Waypoint(cf.create(52, -2.0), 0.0f, (float) 0.0, "New", "Name");

			int newwpID = ai.wpdb.add(newwp);
			ai.setSelectedWaypointID(newwpID);
			// Start edit activity
			try {
				Intent intent = new Intent(this, EditWaypointActivity.class);
				intent.putExtra(EditWaypointActivity.WAYPOINT_BUNDLE, wj.toJson(newwp).toString());
				startActivityForResult(intent, FORCE_WAYPOINT_CHANGE);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;

		case R.id.filterWaypointGoButton:
			scrollToUserInput();
			break;

		default:
			break;
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item.getMenuInfo();

		ListView lv = (ListView) findViewById(R.id.waypointListView);
		int waypointID = getWaypointIDFromAdapter(lv, acmi.position);

		switch (item.getItemId()) {
		case R.id.deleteWaypoint:
			ai.wpdb.deleteWaypoint(waypointID);
			ai.saveWaypointDB();
			setupNewListAdapter();
			lv.setSelection(acmi.position);
			break;

		case R.id.editWaypoint:
			Intent intent = new Intent(this, EditWaypointActivity.class);
			ai.setSelectedWaypointID(waypointID);
			Waypoint wp = ai.wpdb.getWaypoint(waypointID);

			try {
				intent.putExtra(EditWaypointActivity.WAYPOINT_BUNDLE, wj.toJson(wp).toString());
				startActivityForResult(intent, 0);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;

		default:
			return super.onContextItemSelected(item);
		}
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("CREATE", "onCreate");

		setContentView(R.layout.activity_select_waypoint);

		// Set up click handlers
		findViewById(R.id.newWaypointButton).setOnClickListener(this);
		findViewById(R.id.filterWaypointGoButton).setOnClickListener(this);

		ListView lv = (ListView) findViewById(R.id.waypointListView);
		lv.setOnItemClickListener(this);

		EditText ev = (EditText) findViewById(R.id.filterWaypointText);
		ev.setOnEditorActionListener(this);

		ai = ((AviatorApp) getApplication()).i();
		setupNewListAdapter();
		listScrollTo = -1;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_selectwaypoint, menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onEditorAction(TextView filterText, int arg1, KeyEvent arg2) {

		Log.d("EDITOR", "click");
		if (validateWaypointInput()) {

			/*
			 * user has provided a complete matching ident, and pressed return -
			 * we can return this result straight away.
			 */

			returnWaypointResult(getEnteredWaypointIDs());
			return true;
		}
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> av, View arg1, int arg2, long selection) {
		int waypointID = getWaypointIDFromAdapter(av, selection);

		returnWaypointResult(waypointID);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		switch (item.getItemId()) {
		case R.id.exportSD:
			ai.saveWaypointDB();
			break;

		case R.id.importSD:
			ai.loadWaypointDB();
			ai.saveWaypointDB();
			break;

		case R.id.sortByName:
			displayByNames = true;
			setupNewListAdapter();
			break;

		case R.id.sortByIdent:
			displayByNames = false;
			setupNewListAdapter();
			break;

		default:
			return super.onMenuItemSelected(featureId, item);

		}

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		getMenuInflater().inflate(R.menu.activity_select_waypoint, menu);
		menu.removeItem(displayByNames ? R.id.sortByName : R.id.sortByIdent);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// Intentionally blank
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

		ListView list = (ListView) findViewById(R.id.waypointListView);
		EditText et = (EditText) findViewById(R.id.filterWaypointText);

		super.onWindowFocusChanged(hasFocus);
		Log.d("FOCUS", Boolean.toString(hasFocus));
		if (hasFocus) {
			if (listScrollTo > -1) {
				Log.d("FOCUSSCROLL", Integer.toString(listScrollTo));
				list.setSelection(listScrollTo);
				listScrollTo = -1;
			} else {
				// scrollToUserInput(et.getText().toString());
			}
		}
	}

	private int[] getEnteredWaypointIDs() {
		EditText et = (EditText) findViewById(R.id.filterWaypointText);
		String strs[] = et.getText().toString().trim().split(" ");
		int results[] = new int[strs.length];
		int j;

		for (j = 0; j < strs.length; j++) {
			results[j] = ai.wpdb.findByIdent(strs[j]);
		}
		return results;
	}

	/*
	 * check if the supplied IDs are valid waypoint IDs
	 */

	/*
	 * Find the position of the specified string in the current list (ident
	 * sorted, or name sorted). Negative result means no exact match found, but
	 * the magnitude indicates the position it would go in front of.
	 */
	private int getListPosition(String str) {
		final int pos = displayByNames ? ai.wpdb.findByName(str) : ai.wpdb.findByIdent(str);
		return pos;
	}

	/*
	 * return list of the waypoint ids that the user has entered, some of the
	 * results may be <0 if the identifier can't be matched to a waypoint
	 */

	private int getListPosition(Waypoint wp) {
		int wpID = ai.wpdb.findByIdent(wp.getIdent());

		Log.d("GETLIST", Integer.toString(wpID) + wp.getIdent());
		if (displayByNames) {
			int id2 = ai.wpdb.identIDtoNameID(wpID);
			return id2;
		}
		return wpID;
	}

	/*
	 * take a space-delimited string of waypoint identifiers, and return an
	 * array of waypoint IDs.
	 */

	/*
	 * return the database ordered according to which mode we're in
	 */
	private ArrayList<Waypoint> getWaypointDB() {
		return displayByNames ? ai.wpdb.getWaypointsByName() : ai.wpdb.getWaypoints();
	}

	private int getWaypointIDFromAdapter(AdapterView<?> av, long listPosition) {
		Waypoint wp = (Waypoint) av.getItemAtPosition((int) listPosition);
		int waypointID = ai.wpdb.getWaypoints().indexOf(wp);
		return waypointID;
	}

	private void returnWaypointResult(int waypointID) {
		int r[] = { waypointID };
		returnWaypointResult(r);
	}

	private void returnWaypointResult(int[] waypointIDs) {
		Intent i = new Intent();
		i.putExtra(SELECTED_WAYPOINT, waypointIDs);
		setResult(RESULT_OK, i);
		finish();
	}

	private void scrollToUserInput() {
		EditText et = (EditText) findViewById(R.id.filterWaypointText);
		scrollToUserInput(et.getText().toString());
	}

	private void scrollToUserInput(String str) {
		final ListView lv = (ListView) findViewById(R.id.waypointListView);
		if (str.length() == 0 || lv.getChildAt(0) == null) {
			lv.setSelection(0);
			return;
		}
		String strs[] = str.trim().split(" ");
		final int pos = getListPosition(strs[strs.length - 1]);
		final int yoffset;
		final int pos1;

		if (pos >= 0) {

			/*
			 * absolute match, set the top item in the list
			 * 
			 * Also, if the children haven't been created yet then we can't do
			 * the offset calculation
			 */

			pos1 = pos;
			yoffset = 0;

		} else {

			/*
			 * near-match, set the top item but offset it so that it shows only
			 * a portion of the item after which str would be inserted into the
			 * list, as a visual cue to the user.
			 */

			View v = lv.getChildAt(0);
			pos1 = -(pos + 1);
			yoffset = 11 * v.getHeight() / 16;
		}

		/*
		 * TODO: Android funny. For some unknown reason this post-method to set
		 * the list selection is necessary when the user has entered spaces
		 * between waypoint idents, otherwise the list view doesn't update as
		 * the user types. Yet it works fine up until the string has a space in
		 * it?
		 * 
		 * Update: Seems related to setting the QuickAdd text line visible in
		 * afterTextChanged. Removed the quick-add text line and just setting
		 * the error on the input field has worked around the issue, but it
		 * remains a mystery as to why. Perhaps to do with the layout engine
		 * being invoked?
		 */

		lv.setSelectionFromTop(pos1, yoffset);
		// lv.post(new Runnable() {
		// @Override
		// public void run() {
		// // TODO Auto-generated method stub
		// lv.setSelectionFromTop(pos1, yoffset);
		//
		// }
		// });

	}

	private void setupNewListAdapter() {
		ArrayList<Waypoint> waypointList = getWaypointDB();
		ListView lv = (ListView) findViewById(R.id.waypointListView);
		lv.setAdapter(null);

		listAdapter = new ArrayAdapter<Waypoint>(this, R.layout.simplerow, waypointList);
		lv.setAdapter(listAdapter);
		Log.d("ADAPTER", "Created");

	}

	/**
	 * @return
	 * 
	 */
	private boolean validateWaypointInput() {
		EditText et = (EditText) findViewById(R.id.filterWaypointText);
		boolean valid = waypointsIDsValid(getEnteredWaypointIDs());
		et.setError(valid ? null : "Invalid ident");
		return valid;
	}

	private boolean waypointsIDsValid(int[] ids) {
		boolean invalidID = false;
		for (int id : ids) {
			invalidID |= (id < 0);
		}
		return !invalidID;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		Log.d("Result:", Integer.toString(resultCode));
		Log.d("Req:", Integer.toString(requestCode));

		if (requestCode == FORCE_WAYPOINT_CHANGE) {
			resultCode = EditWaypointActivity.WAYPOINT_CHANGED;
		}

		JSONObject json;
		Waypoint wp = null;
		Bundle b = data.getExtras();
		try {
			json = new JSONObject(b.getString(EditWaypointActivity.WAYPOINT_BUNDLE));
//			wp = new Waypoint(json);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		switch (resultCode) {

		case EditWaypointActivity.WAYPOINT_CHANGED:
			Log.d("Bundle:", b.getString(EditWaypointActivity.WAYPOINT_BUNDLE));
			ai.wpdb.setWaypoint(ai.getSelectedWaypointID(), wp);
			ai.saveWaypointDB();
			ai.setSelectedWaypointID(CoreApplication.WAYPOINT_NOT_SELECTED);
			// listScrollTo = getListPosition(wp);
			setupNewListAdapter();
			ListView lv = (ListView) findViewById(R.id.waypointListView);
			lv.setSelection(getListPosition(wp));
			break;

		default:
			/*
			 * don't need to scroll, as this activity wasn't destroyed
			 */
			// listScrollTo = getListPosition(wp);
			break;
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d("PAUSE", getLocalClassName());

	}

	@Override
	protected void onResume() {
		Log.d("RESUME", "onResume select");
		ListView list = (ListView) findViewById(R.id.waypointListView);
		EditText et = (EditText) findViewById(R.id.filterWaypointText);

		registerForContextMenu(list);
		super.onResume();

		/*
		 * call this after super.onResume to stop afterTextChanged from being
		 * called during the state restore
		 */
		et.addTextChangedListener(this);

		et.selectAll();
	}
}
