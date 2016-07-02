package com.gpsaviator.encoders.json;

import com.gpsaviator.Airspace;
import com.gpsaviator.Altitude;
import com.gpsaviator.Boundary;
import com.gpsaviator.CoordinateFactory;
import com.gpsaviator.encoders.Jsoniser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by khaines on 09/12/14.
 */
public class AirspaceJsoniser implements Jsoniser<Airspace> {

    private static final String _JSON_LOWER = "lower";
    private static final String _JSON_UPPER = "upper";
    private static final String _JSON_AIRSPACE_CLASS = "class";
    private static final String _JSON_NAME = "name";
    private static final String _JSON_NOTES = "notes";
    private static final String _JSON_BOUNDS = "bounds";

    private final BoundaryJsoniser bj;

    public AirspaceJsoniser(CoordinateFactory coordinateFactory) {
        this.bj = new BoundaryJsoniser(coordinateFactory);
    }

    @Override
    public JSONObject toJson(Airspace a) throws JSONException {
        JSONObject j = new JSONObject();

        j.put(_JSON_NAME, a.getName());
        j.put(_JSON_NOTES, a.getNotes());
        j.put(_JSON_LOWER, a.getLower().toString());
        j.put(_JSON_UPPER, a.getUpper().toString());
        j.put(_JSON_AIRSPACE_CLASS, a.getAsClass().toString());

        JSONArray bounds = new JSONArray();
        for (Boundary b : a.getBoundaries()) {
            bounds.put(bj.toJson(b));
        }
        j.put(_JSON_BOUNDS, bounds);
        return j;
    }

    @Override
    public Airspace fromJson(JSONObject j) throws JSONException {
        Airspace.AsClass ac;
        try {
            ac = Airspace.AsClass.valueOf(j.getString(_JSON_AIRSPACE_CLASS));
        } catch (Exception e) {
            ac = Airspace.AsClass.Other;
        }
        Altitude lower = new Altitude(j.getString(_JSON_LOWER));
        Altitude upper = new Altitude(j.getString(_JSON_UPPER));
        String name = j.getString(_JSON_NAME);
        String notes = j.getString(_JSON_NOTES);

        ArrayList<Boundary> boundList = new ArrayList<Boundary>();

        JSONArray jBounds = j.getJSONArray(_JSON_BOUNDS);
        for (int i = 0; i < jBounds.length(); i++) {
            boundList.add(bj.fromJson(jBounds.getJSONObject(i)));
        }

        Boundary[] boundaries = boundList.toArray(new Boundary[boundList.size()]);

        return new Airspace(ac, name, notes, boundaries, lower, upper);
    }
}
