package com.gpsaviator;

import lombok.Getter;
import lombok.Setter;

public class Altitude  {

	public enum AltType {

		FLIGHTLEVEL, ALTITUDE, ABOVEGROUND,

	};

	@Getter
	@Setter
	private AltType type;

	@Getter
	private int alt;

	public Altitude(AltType t, int a) {
		set(t, a);
	}

	public Altitude(String str) {
		switch (str.charAt(0)) {
		case 'F':
			type = AltType.FLIGHTLEVEL;
			alt = Integer.valueOf(str.substring(1)) * 100;
			break;

		case 'G':
			type = AltType.ABOVEGROUND;
			alt = Integer.valueOf(str.substring(1));
			break;

		case 'A':
		default:
			type = AltType.ALTITUDE;
			alt = Integer.valueOf(str.substring(1));
			break;
		}
	}

	public String getAsString() {
		switch (type) {
		case FLIGHTLEVEL:
			return String.format("FL%d", alt / 100);

		case ALTITUDE:
			return String.format("%d", alt);

		case ABOVEGROUND:
			return String.format("%dAGL", alt);

		default:
			break;
		}
		return null;
	}

	void set(AltType newType, int newAlt) {
		type = newType;
		alt = newType == AltType.FLIGHTLEVEL ? newAlt * 100 : newAlt;
	}

	@Override
	public String toString() {
		switch (type) {
		case FLIGHTLEVEL:
			return "F" + (Integer.toString(alt / 100));

		case ALTITUDE:
			return "A" + (Integer.toString(alt));

		case ABOVEGROUND:
			return "G" + (Integer.toString(alt));
		}
		return null;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Altitude)) {
			return false;
		}

		Altitude obj1 = (Altitude) obj;
		return (obj1.alt == alt && obj1.type == type);
	}
}
