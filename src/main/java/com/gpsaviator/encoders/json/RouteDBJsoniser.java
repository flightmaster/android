package com.gpsaviator.encoders.json;

import com.gpsaviator.JsonConst;
import com.gpsaviator.Route;
import com.gpsaviator.RouteDB;
import com.gpsaviator.encoders.Jsoniser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

/**
 * Created by khaines on 06/12/2014.
 *
 */
public class RouteDBJsoniser implements Jsoniser<RouteDB> {

    private Jsoniser<Route> rj;

    RouteDBJsoniser(Jsoniser<Route> rj) {
        this.rj = rj;
    }

    @Override
    public JSONObject toJson(RouteDB db) throws JSONException {
        JSONObject jo = new JSONObject();
        JSONArray ja = new JSONArray();
        for (String id : db.getRouteIds()) {
            ja.put(rj.toJson(db.getRoute(id).getRoute()));
        }
        jo.put(JsonConst._JSON_ROUTEDB, ja);
        return jo;
    }

    @Override
    public RouteDB fromJson(JSONObject jsonObject) throws JSONException {
        RouteDB db = new RouteDB();
        JSONArray ja = jsonObject.getJSONArray(JsonConst._JSON_ROUTEDB);
        for (int i = 0; i < ja.length(); i++) {
            db.addRoute(rj.fromJson(ja.getJSONObject(i)));
        }
        return db;
    }
}
