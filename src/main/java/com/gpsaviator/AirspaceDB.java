package com.gpsaviator;

import java.util.*;

import lombok.Getter;

import android.util.Log;

public class AirspaceDB  {

    private static final String LOGTAG = "AirspaceDB";

    private static final int MAX_LIMIT = 1000;

    @Getter
    private List<Airspace> airspace = null;

    public AirspaceDB(int size) {
        airspace = new ArrayList<Airspace>(size);
    }

    /**
     * Return a list of airspaces contained in the specified bounds, up to a maximum limit.
     * If the set of possible airspaces is greater than the limit, then the largest airspaces are
     * returned in the results.
     *
     * @param bounds
     * @return
     */
    public Collection<Airspace> getWithinBounds(Bounds bounds, int limit) {
        Log.d(LOGTAG, "start scan" + bounds);
        List<Airspace> results = new ArrayList<Airspace>(limit);
        for (Airspace a : bounds.filter(airspace)) {
            if (a.getLower().getAlt() > 10000) {
                continue;
            }
            results.add(a);
        }
        if (results.size() > limit) {
            try {
                Collections.sort(results);
            } catch (Exception e) {
                Log.d(LOGTAG, "Sort issues!");
                for (Airspace a : results) {
                    Log.d(LOGTAG, String.format("%s %f", a.getName(), a.getBounds().getArea()));
                }
            }
            results = results.subList(0, limit);
        }
        Log.d(LOGTAG, String.format("end scan, including %d / %d", results.size(), airspace.size()));
        return results;
    }

    public Collection<Airspace> getWithinBounds(Bounds bounds) {
        return getWithinBounds(bounds, MAX_LIMIT);
    }

    public void add(Airspace a) {
        airspace.add(a);
    }

}
