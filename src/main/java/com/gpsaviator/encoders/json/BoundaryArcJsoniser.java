package com.gpsaviator.encoders.json;

import com.gpsaviator.BoundaryArc;
import com.gpsaviator.Coordinate;
import com.gpsaviator.CoordinateFactory;
import com.gpsaviator.encoders.Jsoniser;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by khaines on 08/12/14.
 */
public class BoundaryArcJsoniser implements Jsoniser<BoundaryArc> {

    private static final String _JSON_CENTRE = "centre";
    private static final String _JSON_END = "end";
    private static final String _JSON_START = "start";

    private static final String _JSON_RIGHT = "R";
    private static final String _JSON_LEFT = "L";
    private static final String _JSON_DIRECTION = "dir";

    CoordinateJsonsiser cj;

    BoundaryArcJsoniser(CoordinateFactory cf) {
        this.cj = new CoordinateJsonsiser(cf);
    }

    @Override
    public JSONObject toJson(BoundaryArc wp) throws JSONException {
//        JSONObject j = new JSONObject();
//
//        j.put(_JSON_TYPE, _JSON_ARC);
//        j.put(_JSON_START, start.toJSON());
//        j.put(_JSON_END, end.toJSON());
//        j.put(_JSON_CENTRE, centre.toJSON());
//        j.put(_JSON_DIRECTION, dir == ArcDirection.LEFT ? _JSON_LEFT : _JSON_RIGHT);
//        return j;
        throw new UnsupportedOperationException("Json arc");
    }

    @Override
    public BoundaryArc fromJson(JSONObject j) throws JSONException {
        Coordinate start = cj.fromJson(j.getJSONObject(_JSON_START));
        Coordinate end = cj.fromJson(j.getJSONObject(_JSON_END));
        Coordinate centre = cj.fromJson(j.getJSONObject(_JSON_CENTRE));
        BoundaryArc.ArcDirection dir = (j.getString(_JSON_DIRECTION).equals(_JSON_RIGHT)) ? BoundaryArc.ArcDirection.RIGHT : BoundaryArc.ArcDirection.LEFT;
        return new BoundaryArc(start, end, centre, dir);
    }
}
