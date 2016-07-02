package com.gpsaviator;

/**
 * Created by khaines on 05/12/2014.
 *
 * NOTE: Implementations are assumed to be immutable!
 */
public interface Coordinate {

	/**
	 *
	 * @return Latitude in degrees
	 */
	double getLat();

	/**
	 *
	 * @return Longitude in degrees
	 */
	double getLon();

	/**
	 * Compute bearing between this point and the given point.
	 * @param coord Point to compute bearing to.
	 * @return bearing to given point in radians.
	 */
	double bearingTo(Coordinate coord);

	/**
	 * Compute range from this point to the given point.
	 * @param coord Point to compute range to.
	 * @return Range to the given point in radians.
	 */
	double rangeTo(Coordinate coord);

	/**
	 * Same as rangeTo(coord), except range is returned in the requested units
	 * @param coord Coordinate to measure distance to.
	 * @param units Units to return range in.
	 * @return Range to 'coord' in 'units'.
	 */
	double rangeTo(Coordinate coord, Unit units);

	/**
	 * Returns the coordinate factory that created the coordinate.
	 * @return The coordinate factory.
	 */
	CoordinateFactory getFactory();
}
