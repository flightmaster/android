package com.gpsaviator;

public class LocationData {

	private final float altitude;

	private final Coordinate location;

	private final float speed;

	private final float track;

	/*
	 * track = radians, speed = metres/second, altitude = metres
	 */

	LocationData(Coordinate loc, float track, float speed, float altitude) {
		location = loc;
		this.track = track;
		this.speed = speed;
		this.altitude = altitude;
	}

	public float getAltitude() {
		return altitude;
	}

	public Coordinate getLocation() {
		return location;
	}

	public float getSpeed() {
		return speed;
	}

	public float getTrack() {
		return track;
	}

}
