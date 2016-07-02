package com.gpsaviator;

import lombok.Getter;

public class BoundaryLine extends Boundary {

    @Getter
	private final Coordinate end;

	public BoundaryLine(Coordinate end) {
		this.end = end;
	}

	@Override
	public Coordinate[] expandBounds() {
		Coordinate[] cs = { end };
		return cs;
	}

}
