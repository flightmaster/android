package com.gpsaviator;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by khaines on 22/12/2014.
 */
public class CoordinateDefault extends AbstractCoordinate {

    private double lat;     // degrees
    private double lon;     // degrees

    /**
     * Package private constructor
     * @param lat
     * @param lon
     */
    CoordinateDefault(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public double getLat() {
        return lat;
    }

    @Override
    public double getLon() {
        return lon;
    }

    @Override
    public CoordinateFactory getFactory() {
        return CoordinateDefaultFactory.getInstance();
    }

}
