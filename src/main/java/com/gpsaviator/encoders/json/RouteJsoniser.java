package com.gpsaviator.encoders.json;

import com.gpsaviator.Coordinate;
import com.gpsaviator.JsonConst;
import com.gpsaviator.Route;
import com.gpsaviator.Route.RouteWaypoint;
import com.gpsaviator.encoders.Jsoniser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by khaines on 06/12/2014.
 */
public class RouteJsoniser implements Jsoniser<Route> {

    private final Jsoniser<RouteWaypoint> rwj;

    public class RouteWaypointJsoniser implements Jsoniser<RouteWaypoint> {

        private final Jsoniser<Coordinate> cj;

        public RouteWaypointJsoniser(CoordinateJsonsiser cj) {
            this.cj = cj;
        }

        @Override
        public JSONObject toJson(RouteWaypoint wp) throws JSONException {
            JSONObject json = new JSONObject();
            json.put(JsonConst._JSON_COORD, cj.toJson(wp.getCoord()));
            json.put(JsonConst._JSON_MAGVAR, wp.getMagVar());
            json.put(JsonConst._JSON_IDENT, wp.getIdent());
            json.put(JsonConst._JSON_NAME, wp.getNotes());
            return json;
        }

        @Override
        public RouteWaypoint fromJson(JSONObject json) throws JSONException {
            String ident = json.getString(JsonConst._JSON_IDENT);
            Coordinate coord = cj.fromJson(json.getJSONObject(JsonConst._JSON_COORD));
            String name = json.optString(JsonConst._JSON_NAME);
            float magVar = (float) json.getDouble(JsonConst._JSON_MAGVAR);

            return RouteWaypoint.make(coord, magVar, ident, name);
        }
    }

    public RouteJsoniser(CoordinateJsonsiser coordinateJsonsiser) {
        this.rwj = new RouteWaypointJsoniser(coordinateJsonsiser);
    }

    @Override
    public JSONObject toJson(Route wp) throws JSONException {
        JSONArray ja = new JSONArray();
        JSONObject json = new JSONObject();
        for (RouteWaypoint rw : wp.getPoints()) {
            ja.put(rwj.toJson(rw));
        }
        json.put(JsonConst._JSON_ROUTE, ja);
        return json;
    }

    @Override
    public Route fromJson(JSONObject json) throws JSONException {
        if (json == null) {
            return null;
        }
        Route r = Route.create();
        JSONArray ja = json.getJSONArray(JsonConst._JSON_ROUTE);
        for (int j = 0; j < ja.length(); j++) {
            r.addPoint(rwj.fromJson(ja.getJSONObject(j)));
        }
        return r;
    }
}
