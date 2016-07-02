package com.gpsaviator.android.controller;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import com.gpsaviator.android.AviatorApp;

class SavedActivity extends Activity {

	SavedActivity() {
		super();
	}

	@Override
	protected void onPause() {
		Log.d("PAUSE", isFinishing() ? "Finishing" : "Not finished");
		super.onPause();
		Bundle b = new Bundle();

		/*
		 * only save a new state bundle if this activity is dying, we then
		 * restore this state when our onResume is called
		 */
		if (isFinishing()) {
			Log.d("PAUSE", "SavedActivity Autosave");
			onSaveInstanceState(b);
			((AviatorApp) getApplication()).saveActivityState(getLocalClassName(), b);
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		/*
		 * The system is restoring itself from a killed process. In this case,
		 * onSavedInstanceState will have been used as the saving mechanism, so
		 * our state isn't needed and we should clear it before our onResume
		 * gets called.
		 */
		((AviatorApp) getApplication()).saveActivityState(getLocalClassName(), null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume() Note that any child classes that
	 * override this method must call super.onResume *after* doing their other
	 * initialisations.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		Bundle b = ((AviatorApp) getApplication()).getActivityState(getLocalClassName());
		if (b != null) {
			Log.d("RESUME", "SavedActivity Autoload");
			onRestoreInstanceState(b);

			/*
			 * restored our state, now destroy it so that it's not used again in
			 * the next call to onResume
			 */
			((AviatorApp) getApplication()).saveActivityState(getLocalClassName(), null);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		((AviatorApp) getApplication()).saveActivityState(getLocalClassName(), null);
		super.onSaveInstanceState(outState);
	}

}
