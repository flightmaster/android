package com.gpsaviator;

public class Navaid extends Waypoint {

	public enum Type {
		VOR, DME, VORDME
	};

	private final int frequency;
	private Type type;

	public Navaid(Coordinate coord, float magVar, float alt, String ident,
			String name, int freq) {
		super(coord, magVar, alt, ident, name);
		this.frequency = freq;
	}

	public int getFrequency() {
		return frequency;
	}

	public Type getType() {
		return type;
	}

}
