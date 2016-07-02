package com.gpsaviator.encoders.json;

import com.gpsaviator.Boundary;
import com.gpsaviator.CoordinateFactory;
import com.gpsaviator.encoders.Jsoniser;
import lombok.Getter;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by khaines on 08/12/14.
 */
public class BoundaryJsoniser implements Jsoniser<Boundary> {

    private final BoundaryCircleJsoniser circleJsoniser;
    private final BoundaryLineJsoniser lineJsoniser;
    private final BoundaryArcJsoniser arcJsoniser;

    static final String _JSON_TYPE = "type";
    static final String _JSON_LINE = "line";
    static final String _JSON_CIRCLE = "circle";
    static final String _JSON_ARC = "arc";


    BoundaryJsoniser(CoordinateFactory cf) {
        this.arcJsoniser = new BoundaryArcJsoniser(cf);
        this.circleJsoniser = new BoundaryCircleJsoniser(cf);
        this.lineJsoniser = new BoundaryLineJsoniser(cf);
    }

    @Override
    public JSONObject toJson(Boundary wp) throws JSONException {
        return null;
    }

    @Override
    public Boundary fromJson(JSONObject j) throws JSONException {
        String type = j.getString(_JSON_TYPE);
        if (type.equals(_JSON_LINE)) {
            return lineJsoniser.fromJson(j);
        } else if (type.equals(_JSON_CIRCLE)) {
            return circleJsoniser.fromJson(j);
        } else if (type.equals(_JSON_ARC)) {
            return arcJsoniser.fromJson(j);
        }
        throw new JSONException("Can't decode JSON boundary");
    }
}
