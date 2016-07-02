package com.gpsaviator;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * Represents bounds in radian-based Coordinates.
 * Created by khaines on 27/05/2014.
 */
public class Bounds {

    @Getter private final double swLat;
    @Getter private final double swLon;
    @Getter private final double neLat;
    @Getter private final double neLon;

    public interface Insider {
        public boolean isCoveredBy(Bounds bounds);
    }

    /**
     * Compute an indicative area of the bounds; for relative comparison with other bounds,
     * not accurate in any way.
     * @return Area
     */
    public double getArea() {
        return (neLat - swLat) * (neLon - swLon);
    }

    /**
     * Use the builder class to build a Bounds object progressively from one or
     * more Coordinates.
     */

    public static class Builder {
        private double northEastLat = -90;
        private double northEastLon = -180;
        private double southWestLat = 90;
        private double southWestLon = 180;

        public Builder(Coordinate southWest, Coordinate northEast) {
            southWestLat = southWest.getLat();
            southWestLon = southWest.getLon();
            northEastLat = northEast.getLat();
            northEastLon = northEast.getLon();
        }

        public Builder() {
        }

        public Builder including(Coordinate c) {
            includingLat(c.getLat());
            includingLon(c.getLon());
            return this;
        }

        public Builder includingLat(double lat) {
            if (lat > northEastLat) {
                northEastLat = lat;
            }
            if (lat < southWestLat) {
                southWestLat = lat;
            }
            return this;
        }

        public Builder includingLon(double lon) {
            if (lon > northEastLon) {
                northEastLon = lon;
            }
            if (lon < southWestLon) {
                southWestLon = lon;
            }
            return this;
        }

        public Builder including(Coordinate... cs) {
            for (Coordinate c : cs) {
                including(c);
            }
            return this;
        }

        public Bounds build() {
            return new Bounds(southWestLat, southWestLon, northEastLat, northEastLon);
        }

        public Builder including(Bounds bounds) {
            includingLat(bounds.swLat);
            includingLon(bounds.swLon);
            includingLat(bounds.neLat);
            includingLon(bounds.neLon);
            return this;
        }
    }

    private Bounds(double swLat, double swLon, double neLat, double neLon) {
        this.swLat = swLat;
        this.swLon = swLon;
        this.neLat = neLat;
        this.neLon = neLon;
    }

    /**
     * Test if the given point is within the bounds.
     * @param coordinate
     * @return True if it is.
     */
    public boolean isInside(Coordinate coordinate) {
        return (coordinate.getLat() < neLat &&
                coordinate.getLat() > swLat &&
                coordinate.getLon() < neLon &&
                coordinate.getLon() > swLon);
    }

    public boolean intersects(Bounds b) {
        return ! (b.neLat < swLat
                || b.swLat > neLat
                || b.neLon < swLon
                || b.swLon > neLon);
    }

    /**
     * Filter the collection of Insider objects against the bounds.
     *
     * @param collection
     * @return
     */
    public <E extends Insider> Collection<E> filter(Collection<E> collection) {
        List<E> results = new ArrayList<E>();
        for (E i : collection) {
            if (i.isCoveredBy(this)) {
                results.add(i);
            }
        }
        return results;
    }

    @Override
    public String toString() {
        return String.format("southWest: %f %f northEast: %f %f", swLat, swLon, neLat, neLon );
    }

    @Override
    public boolean equals(Object o) {
        Bounds bo = (Bounds) o;
        return (bo.neLat == neLat && bo.neLon == neLon && bo.swLat == swLat && bo.swLon == swLon);
    }
}
