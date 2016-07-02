package com.gpsaviator.encoders;

import com.gpsaviator.Waypoint;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by khaines on 05/12/2014.
 */
public interface Jsoniser<N> {

    JSONObject toJson(N wp) throws JSONException;

    N fromJson(JSONObject jsonObject) throws JSONException;

}
