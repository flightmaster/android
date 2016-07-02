package com.gpsaviator;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by khaines on 24/12/2014.
 *
 * Provides a base implementation of bearing and range functions for Coordinate implementations.
 */
public abstract class AbstractCoordinate implements Coordinate {

    @Override
    public double bearingTo(Coordinate coord) {
        double lat1 = Math.toRadians(getLat());
        double lon1 = Math.toRadians(-getLon());
        double lat2 = Math.toRadians(coord.getLat());
        double lon2 = Math.toRadians(-coord.getLon());
        double tc1;

        double d = rangeTo(coord);

        if (lon2 - lon1 == 0) {
            return (lat2 > lat1 ? 0 : Math.PI);
        }

        if (Math.sin(lon2 - lon1) < 0) {
            tc1 = Math.acos((Math.sin(lat2) - Math.sin(lat1) * Math.cos(d)) / (Math.sin(d) * Math.cos(lat1)));
        } else {
            tc1 = 2 * Math.PI
                    - Math.acos((Math.sin(lat2) - Math.sin(lat1) * Math.cos(d)) / (Math.sin(d) * Math.cos(lat1)));
        }
        if (tc1 < 0) {
            return 2 * Math.PI + tc1;
        }
        return tc1;
    }

    /**
     * Return the range from this coordinate to the specified coordinate
     * @param coord
     * @return Range, in radians.
     */
    @Override
    public double rangeTo(Coordinate coord) {
        double lat1 = Math.toRadians(getLat());
        double lon1 = Math.toRadians(-getLon());
        double lat2 = Math.toRadians(coord.getLat());
        double lon2 = Math.toRadians(-coord.getLon());
        return Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));
    }

    public double rangeTo(Coordinate coord, Unit units) {
        return UnitConversion.convert(rangeTo(coord), Unit.RADIANS, units);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 21).append(getLat()).append(getLon()).toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        AbstractCoordinate oo = (AbstractCoordinate) o;
        if (oo == this) return true;
        return oo.hashCode() == this.hashCode();
    }
}
