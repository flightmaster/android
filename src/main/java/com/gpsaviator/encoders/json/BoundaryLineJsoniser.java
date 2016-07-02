package com.gpsaviator.encoders.json;

import com.gpsaviator.BoundaryLine;
import com.gpsaviator.CoordinateFactory;
import com.gpsaviator.encoders.Jsoniser;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by khaines on 08/12/14.
 */
public class BoundaryLineJsoniser implements Jsoniser<BoundaryLine> {
    private final CoordinateJsonsiser cj;

    private static final String _JSON_COORD = "coord";

    public BoundaryLineJsoniser(CoordinateFactory cf) {
        this.cj = new CoordinateJsonsiser(cf);
    }

    @Override
    public JSONObject toJson(BoundaryLine wp) throws JSONException {
//        JSONObject j = new JSONObject();
//        j.put(_JSON_TYPE, _JSON_LINE);
//        j.put(_JSON_COORD, end.toJSON());
//        return j;

        throw new UnsupportedOperationException("toJson not supported");
    }

    @Override
    public BoundaryLine fromJson(JSONObject jsonObject) throws JSONException {
        return new BoundaryLine(cj.fromJson(jsonObject.getJSONObject(_JSON_COORD)));
    }
}
