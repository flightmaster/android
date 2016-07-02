package com.gpsaviator;

public class Airfield extends Waypoint {

	// Runways

	// Communications
	private Comms comms[];

	public Airfield(Coordinate coord, float magVar, float alt, String ident,
			String name) {
		super(coord, magVar, alt, ident, name);
	}

	public Comms[] getComms() {
		return comms;
	}

}