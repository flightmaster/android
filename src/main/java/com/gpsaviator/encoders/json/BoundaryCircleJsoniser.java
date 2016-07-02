package com.gpsaviator.encoders.json;

import com.gpsaviator.BoundaryCircle;
import com.gpsaviator.Coordinate;
import com.gpsaviator.CoordinateFactory;
import com.gpsaviator.encoders.Jsoniser;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by khaines on 08/12/14.
 */
public class BoundaryCircleJsoniser implements Jsoniser<BoundaryCircle> {

    private static final String _JSON_RADIUS = "radius";
    private static final String _JSON_CENTRE = "centre";
    private final CoordinateJsonsiser cj;

    BoundaryCircleJsoniser(CoordinateFactory cf) {
        cj = new CoordinateJsonsiser(cf);
    }

    @Override
    public JSONObject toJson(BoundaryCircle b) throws JSONException {

        BoundaryCircle bc = (BoundaryCircle) b;
        JSONObject j = new JSONObject();

        j.put(BoundaryJsoniser._JSON_TYPE, BoundaryJsoniser._JSON_CIRCLE);
        j.put(_JSON_RADIUS, bc.getRadius());
        j.put(_JSON_CENTRE, cj.toJson(bc.getCentre()));
        return j;
    }

    @Override
    public BoundaryCircle fromJson(JSONObject json) throws JSONException {
        Coordinate centre = cj.fromJson(json.getJSONObject(_JSON_CENTRE));
        double radius = json.getDouble(_JSON_RADIUS);
        return new BoundaryCircle(centre, radius);
    }
}
