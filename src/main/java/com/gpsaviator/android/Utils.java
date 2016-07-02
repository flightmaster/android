package com.gpsaviator.android;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.view.View;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.gpsaviator.Coordinate;

import static com.gpsaviator.android.model.CoordinateImpl.asLatLng;

public class Utils {

    /*
	 * Class is never intended to be instantiated, use a private default
	 * constructor to enforce this
	 */
    private Utils() {
        throw new AssertionError();
    }

    ;

    public static PolylineOptions toPolyline(Coordinate[] cs) {
        PolylineOptions line = new PolylineOptions();
        ArrayList<LatLng> latlon = new ArrayList<LatLng>(cs.length);

        for (Coordinate c : cs) {
            latlon.add(asLatLng(c));
        }
        line.addAll(latlon);
        return line;
    }

    public static PolygonOptions toPolygon(Coordinate[] cs) {
        PolygonOptions line = new PolygonOptions();
        ArrayList<LatLng> latlon = new ArrayList<LatLng>(cs.length);

        for (Coordinate c : cs) {
            latlon.add(asLatLng(c));
        }
        line.addAll(latlon);
        return line;
    }


    public static LatLngBounds calcLatLngBounds(List<LatLng> points) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng p : points) {
            builder.include(p);
        }
        return builder.build();

    }

    public static void setClickHandler(Activity activity, View.OnClickListener listener, int view) {
        final View v = activity.findViewById(view);
        v.setOnClickListener(listener);
    }

    public static void setClickHandler(Activity activity, View.OnClickListener listener, int[] views) {
        for (int view : views) {
            final View v = activity.findViewById(view);
            v.setOnClickListener(listener);
        }
    }

    public static void setClickHandler(Activity activity, View.OnClickListener listener, View.OnLongClickListener longListener, int[] views) {
        for (int view : views) {
            final View viewById = activity.findViewById(view);
            viewById.setOnClickListener(listener);
            viewById.setOnLongClickListener(longListener);
        }
    }


}
