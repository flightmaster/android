package com.gpsaviator.encoders.json;

import com.gpsaviator.Coordinate;
import com.gpsaviator.InfoMap;
import com.gpsaviator.JsonConst;
import com.gpsaviator.Waypoint;
import com.gpsaviator.CoordinateFactory;
import com.gpsaviator.encoders.Jsoniser;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by khaines on 05/12/2014.
 */
public class WaypointJsoniser implements Jsoniser<Waypoint> {

    private CoordinateJsonsiser cj;

    public WaypointJsoniser(CoordinateFactory cf) {
        this.cj = new CoordinateJsonsiser(cf);
    }

    @Override
    public JSONObject toJson(Waypoint wp) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JsonConst._JSON_IDENT, wp.getIdent());
        json.put(JsonConst._JSON_COORD, cj.toJson(wp.getCoord()));
        int mv = (int) Math.round(wp.getMagVar() * 100.0);
        json.put(JsonConst._JSON_MAGVAR, mv * 10);
        json.put(JsonConst._JSON_ALT, wp.getAltitude());
        json.put(JsonConst._JSON_INFO, wp.getInfo().toJSON());
        return json;
    }

    @Override
    public Waypoint fromJson(JSONObject json) throws JSONException {
        String ident = json.getString(JsonConst._JSON_IDENT);
        Coordinate coord = cj.fromJson(json.getJSONObject(JsonConst._JSON_COORD));
        float magVar = (float) (json.getInt(JsonConst._JSON_MAGVAR) / 100.0);
        float altitude = (float) json.getDouble(JsonConst._JSON_ALT);

        InfoMap info = new InfoMap(json.optJSONObject(JsonConst._JSON_INFO));

        Waypoint wp = new Waypoint(coord, magVar, altitude, ident, null);
        wp.setInfo(info);
        return wp;
    }
}
