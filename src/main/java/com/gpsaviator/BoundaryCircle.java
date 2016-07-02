package com.gpsaviator;

import lombok.Getter;

public class BoundaryCircle extends Boundary {

    @Getter
    private
    Coordinate centre;

    @Getter
    private
    double radius;  // radius in NM

    public BoundaryCircle(Coordinate centre, double radius) {
        this.centre = centre;
        this.radius = radius;
    }

    @Override
    public Coordinate[] expandBounds() {
        throw new UnsupportedOperationException("Circle cannot expand bounds");
    }

    public Bounds getBounds() {
        Bounds.Builder bounds = new Bounds.Builder();
        bounds.including(getCentre());
        double latDiff = (radius / 60.0);
        double lonDiff = latDiff / Math.cos(Math.toRadians(getCentre().getLat()));
        bounds.includingLat(getCentre().getLat() - latDiff);
        bounds.includingLat(getCentre().getLat() + latDiff);
        bounds.includingLon(getCentre().getLon() - lonDiff);
        bounds.includingLon(getCentre().getLon() + lonDiff);
        return bounds.build();
    }
}
