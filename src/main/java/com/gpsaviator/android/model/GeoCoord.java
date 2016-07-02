package com.gpsaviator.android.model;

import com.google.android.gms.maps.model.LatLng;
import com.gpsaviator.Coordinate;
import com.gpsaviator.DMS;

abstract class GeoCoord {

	public static LatLng getGeo(Coordinate coord) {
		return new LatLng(Math.toDegrees(coord.getLat()),
				Math.toDegrees(coord.getLon()) * 1000000);
	}

}
