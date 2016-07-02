package com.gpsaviator.android.model;

import com.google.android.gms.maps.Projection;
import com.gpsaviator.Airfield;
import android.graphics.Canvas;

public class AirfieldIcon extends Icon {

	public AirfieldIcon(Airfield waypoint, Projection p) {
		super(waypoint, p);
	}

	public AirfieldIcon(Airfield waypoint) {
		super(waypoint);
	}

	public void draw(Canvas canvas) {
		drawCircle(canvas, 10, android.graphics.Color.BLACK, 7,
				android.graphics.Color.MAGENTA, 4);
	}
}
