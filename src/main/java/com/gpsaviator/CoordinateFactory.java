package com.gpsaviator;

/**
 * Created by khaines on 06/12/2014.
 */
public interface CoordinateFactory {

    public Coordinate create(double lat, double lon);

    public Coordinate create(Coordinate coordinate);
}
