package com.gpsaviator.encoders.json;

import com.gpsaviator.Coordinate;
import com.gpsaviator.CoordinateFactory;
import com.gpsaviator.DMS;
import com.gpsaviator.encoders.Jsoniser;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by khaines on 06/12/2014.
 */
public class CoordinateJsonsiser implements Jsoniser<Coordinate> {

    private CoordinateFactory cf;

    public CoordinateJsonsiser(CoordinateFactory coordinateFactory) {
        cf = coordinateFactory;
    }

    @Override
    public JSONObject toJson(Coordinate obj) {
        JSONObject json = new JSONObject();
        DMS dlat = new DMS(obj.getLat());
        DMS dlon = new DMS(obj.getLon());
        try {
            json.put("lat", dlat.toString());
            json.put("lon", dlon.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }

    @Override
    public Coordinate fromJson(JSONObject json) throws JSONException {
        DMS lat = new DMS(json.getString("lat"));
        DMS lon = new DMS(json.getString("lon"));
        return cf.create(lat.toDeg(), lon.toDeg());
    }

}
