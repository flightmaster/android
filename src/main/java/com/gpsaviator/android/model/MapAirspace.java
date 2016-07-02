package com.gpsaviator.android.model;

import android.graphics.Color;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.*;
import com.gpsaviator.Airspace;
import com.gpsaviator.BoundaryCircle;
import com.gpsaviator.Bounds;
import com.gpsaviator.Coordinate;
import com.gpsaviator.android.Utils;
import lombok.Getter;
import lombok.Setter;

/**
* Created by khaines on 19/04/14.
 *
*/
public abstract class MapAirspace implements Comparable {
    @Getter
    private final Airspace airspace;

    @Getter @Setter
    private LatLngBounds bounds;

    @Getter private float area;

    @Getter
    private final Style style;

    public static class Style {
        static Style classA = new Style(Color.argb(255, 128, 0, 0), 0.75f, Color.argb(64,128,0,0));
        static Style classB = new Style(Color.argb(255, 0, 0, 200), 0.5f, Color.argb(48, 0,0,200));
        static Style classC = new Style(Color.argb(255, 0, 0, 200), 0.5f, Color.argb(48, 0,0,200));
        static Style classD = new Style(Color.argb(128, 0, 0, 200), 0.5f, Color.argb(64, 128, 0, 128));
        static Style classG = new Style(Color.argb(255, 0, 0, 200), 0.5f, 0);
        static Style restricted = new Style(Color.argb(255, 200, 0, 0), 0.5f, Color.argb(64, 128, 0, 0));
        static Style warning = new Style(Color.argb(255, 200, 0, 0), 0.4f, Color.argb(48,200,0,0));
        static Style generic = new Style(Color.argb(255, 0, 0, 200), 0.5f, 0);

        int edgeColour;
        float edgeWidthFactor;
        int fillColour;             // only used if down to ground level

        public Style(int edgeColour, float v, int fillColour) {
            this.edgeColour = edgeColour;
            edgeWidthFactor = v;
            this.fillColour = fillColour;
        }
    }

    public static Style getStyle(Airspace a) {
        switch (a.getAsClass()) {
            case A:
                return Style.classA;
            case B:
                return Style.classB;
            case C:
                return Style.classC;
            case D:
                return Style.classD;
            case G:
                return Style.classG;
            case R:
            case SD:
            case SR:
                return Style.restricted;
            case W:
            case SW:
                return Style.warning;
        }
        return Style.generic;
    }


    private static class MapPolygon extends MapAirspace {
        private final PolygonOptions polygonOptions;
        private Polygon polygon;

        MapPolygon(Airspace a) {
            super(a);
            Coordinate[] cs = a.expandBounds();
            polygonOptions = Utils.toPolygon(cs);
            polygonOptions.strokeColor(getStyle().edgeColour);
            polygonOptions.strokeWidth((polygonOptions.getStrokeWidth() * getStyle().edgeWidthFactor));
            polygonOptions.fillColor(getStyle().fillColour);
        }

        @Override
        public void render(GoogleMap map) {
            polygon = map.addPolygon(polygonOptions);
            setBounds(Utils.calcLatLngBounds(polygon.getPoints()));
            super.render(map);
        }

        @Override
        public void setVisible(boolean visible) {
            polygon.setVisible(visible);
        }

        @Override
        public void remove() {
            polygon.remove();
        }
    }

    private static class MapPolyline extends MapAirspace {
        private final PolylineOptions lineOptions;
        private Polyline line;

        MapPolyline(Airspace a) {
            super(a);
            Coordinate[] cs = a.expandBounds();
            lineOptions = Utils.toPolyline(cs);
            lineOptions.color(getStyle().edgeColour);
            lineOptions.width((float) (lineOptions.getWidth() / 2.0));
        }

        @Override
        public void render(GoogleMap map) {
            line = map.addPolyline(lineOptions);
            setBounds(Utils.calcLatLngBounds(line.getPoints()));
            super.render(map);
        }

        @Override
        public void setVisible(boolean visible) {
            line.setVisible(visible);
        }

        @Override
        public void remove() {
            line.remove();
        }
    }

    private static class MapCircle extends MapAirspace {
        private final CircleOptions circleOptions;
        private Circle circle;

        public MapCircle(Airspace a) {
            super(a);
            BoundaryCircle bc = (BoundaryCircle) a.getBoundaries()[0];
            LatLng centre = new LatLng(bc.getCentre().getLat(), bc.getCentre().getLon());
            double radius = bc.getRadius()* 1852;  // convert NM to metres
            circleOptions = new CircleOptions()
                    .center(centre)
                    .fillColor(a.getLower().getAlt() == 0 ? getStyle().fillColour : 0)
                    .strokeColor(getStyle().edgeColour)
                    .radius(radius);
            circleOptions.strokeWidth((circleOptions.getStrokeWidth() * getStyle().edgeWidthFactor));

            Bounds bounds = bc.getBounds();
            setBounds(new LatLngBounds(new LatLng(bounds.getSwLat(), bounds.getSwLon()),
                    new LatLng(bounds.getNeLat(), bounds.getNeLon())));
        }

        @Override
        public void render(GoogleMap map) {
            circle = map.addCircle(circleOptions);
            super.render(map);
        }

        @Override
        public void setVisible(boolean visible) {
            circle.setVisible(visible);
        }

        @Override
        public void remove() {
            circle.remove();
        }
    }

    private MapAirspace(Airspace a) {
        this.airspace = a;
        style = getStyle(a);
    }

    public static MapAirspace getMapAirspace(Airspace a) {
        if (a.isCircle()) {
            return new MapCircle(a);
        }
        if (a.getLower().getAlt() == 0) {
            return new MapPolygon(a);
        } else {
            return new MapPolyline(a);
        }
    }

    public void render(GoogleMap map) {
        area = computeArea(bounds);
    }

    public abstract void setVisible(boolean visible);

    public abstract void remove();


    private float computeArea(LatLngBounds b) {
        return (float) ((b.northeast.latitude - b.southwest.latitude) * (b.northeast.longitude - b.southwest.longitude));
    }

    @Override
    public int compareTo(Object another) {
        MapAirspace other = (MapAirspace) another;
        if (area > other.area)
            return -1;
        else if (area < other.area)
            return 1;
        else return 0;
    }

}
