package com.gpsaviator;

/**
 * Created by khaines on 22/12/2014.
 */
public class CoordinateDefaultFactory implements CoordinateFactory {

    private static CoordinateFactory instance = new CoordinateDefaultFactory();

    private CoordinateDefaultFactory() {}

    public static CoordinateFactory getInstance() {
        return instance;
    }

    @Override
    public Coordinate create(double lat, double lon) {
        return new CoordinateDefault(lat, lon);
    }

    @Override
    public Coordinate create(Coordinate coordinate) {
        return new CoordinateDefault(coordinate.getLat(), coordinate.getLon());
    }

}
