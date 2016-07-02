package com.gpsaviator;

public abstract class Boundary {

	public abstract Coordinate[] expandBounds();

    public Bounds getBounds() {
        Bounds.Builder builder = new Bounds.Builder();
        for (Coordinate c : expandBounds()) {
            builder.including(c);
        }
        return  builder.build();
    }

}
