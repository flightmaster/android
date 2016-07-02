package com.gpsaviator;

import lombok.Getter;

public class BoundaryArc extends Boundary {

    public static final int numPoints = 16;
    private static final double PI = Math.PI;

	public enum ArcDirection {
		LEFT, RIGHT,
	}

	@Getter
	Coordinate start;

	@Getter
	Coordinate end;

	@Getter
	Coordinate centre;

	@Getter
	ArcDirection dir;

	public BoundaryArc(Coordinate start, Coordinate end, Coordinate centre, ArcDirection dir) {
		this.start = start;
		this.end = end;
		this.centre = centre;
		this.dir = dir;
	}


    // TODO: *** Convert to use a different/efficient bounds calculation

	@Override
	public Coordinate[] expandBounds() {
        final double centreLat = centre.getLat();
        final double centreLon = centre.getLon();
        final double startLat = start.getLat();
        final double startLon = start.getLon();
        final double endLat = end.getLat();
        final double endLon = end.getLon();

        double cosLat = Math.cos(Math.toRadians(centre.getLat()));
        double radiusY = Math.toDegrees(start.rangeTo(centre));
        double radiusX = radiusY/cosLat;

        double startAngle = (PI /2) - Math.atan2(startLat - centreLat,
                (startLon - centreLon) * cosLat);
        double endAngle = (PI /2) - Math.atan2(endLat - centreLat,
                (endLon - centreLon) * cosLat);

//        System.out.println("StartAngle " + Double.toString(Math.toDegrees(startAngle)));
//        System.out.println("EndAngle " + Double.toString(Math.toDegrees(endAngle)));

        if (startAngle < -PI) startAngle += 2*PI;
        if (startAngle > PI) startAngle -= 2*PI;

        if (endAngle < -PI) endAngle +=  2*PI;
        if (endAngle > PI) endAngle -= 2*PI;
//        System.out.println("StartAngle2 " + Double.toString(Math.toDegrees(startAngle)));
//        System.out.println("EndAngle2 " + Double.toString(Math.toDegrees(endAngle)));
//
        final int numPoints = 16;
		Coordinate[] cs = new Coordinate[numPoints+2];
        double arc = endAngle - startAngle;

        if (dir == ArcDirection.LEFT && (arc > PI && startAngle < 0 && endAngle > 0)) {
            arc -= 2*PI;
        }
        if (dir == ArcDirection.RIGHT && (arc < -PI && endAngle < 0 && startAngle > 0)) {
            arc += 2*PI;
        }
        double increment = arc / (numPoints + 1);

        double theta = startAngle + increment;
        cs[0] = start;
        CoordinateFactory cf = start.getFactory();
        for (int i = 0; i < numPoints ; i++) {
            cs[i+1] =  cf.create(centreLat + radiusY * Math.cos(theta), centreLon + radiusX * Math.sin(theta));
            theta += increment;
        }
        cs[numPoints+1] = end;
        return cs;
	}

}
