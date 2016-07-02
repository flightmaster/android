/**
 * 
 */
package com.gpsaviator;

import java.util.Comparator;

import lombok.Getter;
import lombok.Setter;

/**
 * @author khaines
 * 
 */

public class Waypoint implements Point, Comparable<Waypoint>, Comparator<Waypoint>, Bounds.Insider {

    static public Comparator<Waypoint> identComparator() {
		return new Comparator<Waypoint>() {
			@Override
			public int compare(Waypoint lhs, Waypoint rhs) {
				return lhs.getIdent().compareToIgnoreCase(rhs.getIdent());
			}
		};
	}

	/**
	 * @return
	 */
	static public Comparator<Waypoint> nameComparator() {
		return new Comparator<Waypoint>() {
			@Override
			public int compare(Waypoint w1, Waypoint w2) {
				return w1.getName().compareToIgnoreCase(w2.getName());
			}
		};
	}

    @Getter
    @Setter
	private  float altitude; // altitude in metres

    @Getter @Setter
	private Coordinate coord;

    @Getter @Setter
	private String ident; // ICAO identifier

    @Getter @Setter
	private InfoMap info;

    @Getter @Setter
	private float magVar; // Magnetic variation in radians

	public Waypoint(Coordinate coord, float magVar, float altitude, String ident, String name) {
		this.coord = coord;
		this.magVar = magVar;
		this.ident = ident;
		this.altitude = altitude;

		info = new InfoMap();
        if (name != null) info.set(InfoMap._NAME, name);
    }

	public Waypoint(Waypoint wp) {
		altitude = wp.altitude;
		coord = wp.getCoord();
		ident = wp.ident;
		magVar = wp.magVar;
        info = new InfoMap(wp.info);
    }

	@Override
	public int compare(Waypoint lhs, Waypoint rhs) {
		int result = lhs.getIdent().compareToIgnoreCase(rhs.getIdent());

		if (result == 0) {
			/*
			 * idents are equal, use the name
			 */
			return lhs.getName().compareToIgnoreCase(rhs.getName());
		}
		return result;
	}

	@Override
	public int compareTo(Waypoint another) {
		return compare(this, another);
	}

	public String getName() {
		return info.getName();
	}

	@Override
	public String toString() {
		String result = null;

		if (ident != null) {
			if (getName() != null) {
				result = ident + " (" + getName() + ")";
			} else {
				result = ident;
			}
			String type = info.getType();
			result = type == null ? result : result + " " + type;
		}

		return result;
	}

    @Override
    public boolean isCoveredBy(Bounds bounds) {
        return bounds.isInside(getCoord());
    }
}