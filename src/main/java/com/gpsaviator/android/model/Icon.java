package com.gpsaviator.android.model;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.gpsaviator.Waypoint;
import static com.gpsaviator.android.model.CoordinateImpl.asLatLng;

public class Icon {

	public static Icon factory(Waypoint wp, Projection p) {
//		if ("com.gpsaviator.Airfield".equals(wp.getClass().getNotes())) {
//			return new AirfieldIcon((Airfield) wp, p);
//		} else {
//			return new Icon(wp, p);
//		}
			return new Icon(wp, p);
	}

	private LatLng geoPoint;
	private Point point;

	private Waypoint waypoint;

	Icon(Waypoint waypoint) {
		setWaypoint(waypoint);
	}

	Icon(Waypoint waypoint, Projection p) {
		setWaypoint(waypoint);
		point = new Point();
		p.toScreenLocation(geoPoint);
	}

	void draw(Canvas canvas) {
		drawSquare(canvas, 10, android.graphics.Color.BLACK, 8,
				android.graphics.Color.GREEN, 4);
	}

	public void draw(Canvas canvas, Projection projection) {

		updateIconPosition(projection);
		draw(canvas);
	}

	void drawCircle(Canvas canvas, int radius, int outerColour,
                    int outerWidth, int innerColour, int innerWidth) {
		Point drawCentre = point;
		Paint mPaint = new Paint();
		mPaint.setDither(true);

		mPaint.setStyle(Paint.Style.STROKE);

		Path path = new Path();

		// projection.toPixels(this.geoPoint, drawCentre);
		mPaint.setStrokeWidth(outerWidth);
		mPaint.setColor(outerColour);
		path.addCircle(drawCentre.x, drawCentre.y, radius, Path.Direction.CW);
		canvas.drawPath(path, mPaint);

		mPaint.setStrokeWidth(innerWidth);
		mPaint.setColor(innerColour);
		path.addCircle(drawCentre.x, drawCentre.y, radius, Path.Direction.CW);
		canvas.drawPath(path, mPaint);

	}

	void drawSquare(Canvas canvas, int width, int outerColour,
                    int outerWidth, int innerColour, int innerWidth) {
		Paint mPaint = new Paint();
		mPaint.setDither(true);
		float w = width;
		float top = point.y - w;
		float left = point.x - w;
		float bottom = point.y + w;
		float right = point.x + w;

		mPaint.setStyle(Paint.Style.STROKE);

		Path path = new Path();

		// projection.toPixels(this.geoPoint, drawCentre);
		mPaint.setStrokeWidth(outerWidth);
		mPaint.setColor(outerColour);
		path.addRect(left, top, right, bottom, Path.Direction.CW);
		canvas.drawPath(path, mPaint);

		mPaint.setStrokeWidth(innerWidth);
		mPaint.setColor(innerColour);
		path.addRect(left, top, right, bottom, Path.Direction.CW);
		canvas.drawPath(path, mPaint);

	}

	public LatLng getGeoPoint() {
		return geoPoint;
	}

	public Point getPoint() {
		return point;
	}

	public Waypoint getWaypoint() {
		return waypoint;
	}

	void setWaypoint(Waypoint waypoint) {
		this.waypoint = waypoint;
		geoPoint = asLatLng(waypoint.getCoord());
		point = null;
	}

	void updateIconPosition(Projection p) {
		p.toScreenLocation(geoPoint);
	}
}
