package com.gpsaviator;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.Arrays;

public class Airspace implements Comparable, Bounds.Insider {

    private Bounds bounds = null;

    public boolean isCircle() {
        return boundaries[0] instanceof BoundaryCircle;
    }

    @Override
    public int compareTo(Object another) {
        if (this.getArea() < ((Airspace) another).getArea()) {
            return -1;
        } else if (this.getArea() > ((Airspace) another).getArea()) {
            return 1;
        }
        return 0;
    }

    private double getArea() {
        return this.getBounds().getArea();
    }

    @Override
    public boolean isCoveredBy(Bounds bounds) {
        return getBounds().intersects(bounds);
    }

    public enum AsClass {
		A, B, C, D, E, F, G, R, W, SW, SD, SR, Other
	}

	@Getter
	private final Altitude upper;

	@Getter
	private final Altitude lower;

	@Getter
	private final String name;

	@Getter
	private final AsClass asClass;

	@Getter
	@Setter
	private String notes;

	@Getter
	private final Boundary[] boundaries;

	public Airspace(AsClass c, String name, String notes, Boundary[] bounds, Altitude lower, Altitude upper) {
		asClass = c;
		this.name = name;
		this.notes = notes;
		this.boundaries = bounds;
		this.lower = lower;
		this.upper = upper;
	}

	public Coordinate[] expandBounds() {
		ArrayList<Coordinate> bs = new ArrayList<Coordinate>(64);

		for (Boundary b : boundaries) {
			bs.addAll(Arrays.asList(b.expandBounds()));
		}
		return bs.toArray(new Coordinate[0]);
	}

    public Bounds getBounds() {
        if (bounds != null) {
            return bounds;
        }

        Bounds.Builder builder = new Bounds.Builder();
        for (Boundary b : boundaries) {
            builder.including(b.getBounds());
        }
        bounds = builder.build();
        return bounds;
    }
}
