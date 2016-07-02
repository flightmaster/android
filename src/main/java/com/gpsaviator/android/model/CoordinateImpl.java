package com.gpsaviator.android.model;

import com.google.android.gms.maps.model.LatLng;
import com.gpsaviator.AbstractCoordinate;
import com.gpsaviator.Coordinate;
import com.gpsaviator.CoordinateFactory;
import com.gpsaviator.DMS;
import lombok.Getter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public final class CoordinateImpl extends AbstractCoordinate {

    @Getter
    private final LatLng latLng;

	private static final CoordinateFactory factory = CoordinateImplFactory.getInstance();

	public static LatLng asLatLng(Coordinate c) {
		return ((CoordinateImpl)c).getLatLng();
	}

	private CoordinateImpl(double lat, double lon) {
        latLng = new LatLng(lat, lon);
	}

    private CoordinateImpl(LatLng ll) {
        latLng = new LatLng(ll.latitude, ll.longitude);
    }

	public static CoordinateImpl createCoordinateImpl(LatLng ll) {
		return new CoordinateImpl(ll);
	}

	@Override
	public double getLat() {
		return latLng.latitude;
	}

	@Override
	public double getLon() {
		return latLng.longitude;
	}


	@Override
	public boolean equals(Object obj) {

		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Coordinate)) {
			return false;
		}

		CoordinateImpl rhs = (CoordinateImpl) obj;
		return new EqualsBuilder().append(latLng, rhs.getLatLng()).isEquals();

	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(19, 21).append(latLng.latitude).append(latLng.longitude).toHashCode();
	}

	@Override
	public String toString() {
		DMS lat1 = new DMS(latLng.latitude);
		DMS lon1 = new DMS(latLng.longitude);
		return lat1.toString() + "," + lon1.toString();
	}

	public static CoordinateImpl create(double lat, double lon) {
		return new CoordinateImpl(lat, lon);
	}


	@Override
	public CoordinateFactory getFactory() {
		return factory;
	}


}