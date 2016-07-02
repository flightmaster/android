package com.gpsaviator.android;

import android.os.AsyncTask;
import android.util.Log;
import com.google.android.gms.maps.GoogleMap;
import com.gpsaviator.Airspace;
import com.gpsaviator.android.model.MapAirspace;
import com.gpsaviator.android.model.MapElements;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
* Created by khaines on 12/06/14.
*/
public class AirspaceTask extends AsyncTask<Void, MapAirspace, Void> {

    private static final String TAG = "AirspaceTask";
    private final OnCompleted listener;

    public interface OnCompleted {
        void onCompleted();
    }

    private final GoogleMap map;
    private final BlockingQueue<Airspace> queue;    // Airspace to add arrives in the queue
    private final MapElements<Airspace, MapAirspace> airspaceElements;

    public AirspaceTask(GoogleMap map, BlockingQueue airspaceQueue,
                 MapElements<Airspace, MapAirspace> elements,
                 OnCompleted listener) {
        this.map = map;
        this.queue = airspaceQueue;
        this.airspaceElements = elements;
        this.listener = listener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d(TAG, "Background");
        while (!isCancelled()) {
            try {
                Airspace a = queue.poll(300, TimeUnit.MILLISECONDS);
                if (a != null) {
                    MapAirspace ma = MapAirspace.getMapAirspace(a);
                    publishProgress(ma);
                    Thread.sleep(10);
                } else {
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.d(TAG, "Queue was interrupted, aborting");
            }
        }
        Log.d(TAG, "Task complete");
        return null;
    }

    @Override
    protected void onProgressUpdate(MapAirspace... values) {
        for (MapAirspace ma : values) {
            final Airspace airspace = ma.getAirspace();
            if (!airspaceElements.exists(airspace)) {
                ma.render(map);
                airspaceElements.add(airspace, ma);
            }
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.d(TAG, "Background airspace finished");
        listener.onCompleted();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        Log.d(TAG, "Background airspace cancelled");
        listener.onCompleted();
    }
}
