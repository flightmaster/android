package com.gpsaviator.android.model;

import com.gpsaviator.Coordinate;
import com.gpsaviator.CoordinateFactory;

/**
 * Created by khaines on 06/12/2014.
 */
public class CoordinateImplFactory implements CoordinateFactory {

    private static CoordinateFactory instance = new CoordinateImplFactory();

    private CoordinateImplFactory() {}

    public static CoordinateFactory getInstance() {
        return instance;
    }

    @Override
    public Coordinate create(double lat, double lon) {
        return CoordinateImpl.create(lat, lon);
    }

    @Override
    public Coordinate create(Coordinate coordinate) {
        return CoordinateImpl.create(coordinate.getLat(), coordinate.getLon());
    }

}
