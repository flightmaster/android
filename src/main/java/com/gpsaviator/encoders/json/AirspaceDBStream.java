package com.gpsaviator.encoders.json;

import android.util.Log;
import com.gpsaviator.Airspace;
import com.gpsaviator.AirspaceDB;
import com.gpsaviator.CoordinateFactory;
import com.gpsaviator.encoders.JsonStreamable;
import lombok.Getter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by khaines on 10/12/14.
 */
public class AirspaceDBStream implements JsonStreamable {

    private static final String LOGTAG = "AirspaceDBStream";

    @Getter
    private AirspaceDB db;
    private CoordinateFactory cf;

    public AirspaceDBStream(AirspaceDB db, CoordinateFactory cf) {
        this.db = db;
        this.cf = cf;
    }

    @Override
    public void readStream(InputStream is) throws IOException, JSONException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        AirspaceJsoniser aj = new AirspaceJsoniser(cf);

        String line;
        Airspace as;
        Log.d(LOGTAG, "read airspace started");

        int lineCounter = 1;

        try {
            while (br.ready()) {
                line = br.readLine();
                db.add(aj.fromJson(new JSONObject(line)));
                lineCounter++;
            }
            Log.d(LOGTAG, String.format("airspace read complete, %d objects", db.getAirspace().size()));
        } catch (Exception e) {
            Log.d(LOGTAG, String.format("Failed to read all of airspace data at line %d, exception: %s", lineCounter, e.toString()));
        }
    }

    @Override
    public void writeStream(OutputStream os) throws IOException, JSONException {
        AirspaceJsoniser aj = new AirspaceJsoniser(cf);
        Log.d("JSON", "write started");
        for (Airspace a : db.getAirspace()) {
            os.write(aj.toJson(a).toString().getBytes());
            os.write(10);
        }
        Log.d("JSON", "write airspace completed");
    }
}
