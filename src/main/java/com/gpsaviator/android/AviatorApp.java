package com.gpsaviator.android;

import java.util.HashMap;

import android.app.Application;
import android.os.Bundle;

import com.gpsaviator.CoreApplication;

public class AviatorApp extends Application {

	private CoreApplication core;

	private final HashMap<String, Bundle> activitySavedState;

	public AviatorApp() {
		super();
		activitySavedState = new HashMap<String, Bundle>();
	}

	public Bundle getActivityState(String name) {

		Bundle b = activitySavedState.get(name);
		if (b != null) {
			// activitySavedState.remove(name);
		}
		return b;
	}

	public CoreApplication i() {
		return core;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		core = new CoreAndroid(this);
	}

	public void saveActivityState(String name, Bundle b) {
		activitySavedState.put(name, b);
	}
}
