package com.gpsaviator.android;

import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.*;

/**
 *
 */
public class WaypointNamer {
    private static final int MAX_ADDRESSES = 7;
    public static final String NO_ADDRESS_DATA = "No Address Data";
    private final LatLng position;
    private int lastSelection;
    private List<String> items;
    private String defaultName;

    /**
     * Create a new WaypointNamer instance using the position and geocoder provided.
     *
     * @param newPos      position to query.
     * @param geo         Geocoder to use.
     * @param defaultName
     * @return A new WaypointNamer instance.
     */
    public static WaypointNamer position(LatLng newPos, Geocoder geo, String defaultName) {
        WaypointNamer namer = new WaypointNamer(newPos);

        namer.items = new ArrayList<String>();
        namer.defaultName = defaultName;

        if (!Geocoder.isPresent()) {
            return namer;
        }

        try {
            // We query the location for MAX_ADDRESSES items, then look through the various
            // strings that come back. The strings are sorted by frequency and the most frequent
            // occurrences will be near the top of the list. There are a few heuristics used to
            // help sift the strings.

            List<Address> addresses = geo.getFromLocation(namer.position.latitude, namer.position.longitude, MAX_ADDRESSES);
            Map<String, Integer> frequencies = new HashMap<String, Integer>();
            for (Address a : addresses) {
                final int maxAddressLineIndex = a.getMaxAddressLineIndex();

                // don't use postal-code strings.
                if (!a.getFeatureName().equals(a.getPostalCode())) {
                    updateHash(frequencies, a.getFeatureName());

                    // The feature name seems to warrant more weight if there are 3 or less lines in the address
                    // array.
                    if (maxAddressLineIndex < 4) {
                        updateHash(frequencies, a.getFeatureName());
                    }
                }
                updateHash(frequencies, a.getLocality());
                updateHash(frequencies, a.getAdminArea());

                // Add some of the address line elements.
                for (int idx = 0; idx < maxAddressLineIndex; idx++) {
                    final String addressLine = a.getAddressLine(idx);
                    final String adminArea = a.getAdminArea();

                    // don't add strings from the address lines that match the admin area, otherwise
                    // these get too much weight.
                    if (adminArea == null || !adminArea.equals(addressLine)) {
                        updateHash(frequencies, addressLine);
                    }
                }
//                    Log.d("WaypointNamer", a.toString());
            }

            // Now sort the elements of the map into a list of map.entry elements, using the
            // value from the map (i.e. number of occurrences) as the sort key.
            ArrayList<Map.Entry<String, Integer>> freqList = new ArrayList<Map.Entry<String, Integer>>(frequencies.entrySet());
            Collections.sort(freqList, new Comparator<Map.Entry<String, Integer>>() {
                public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                    // compare o2 to o1, instead of o1 to o2, to get descending freq. order
                    return (o2.getValue()).compareTo(o1.getValue());
                }
            });
//                Log.d("WaypointNamer", freqList.toString());
            for (Map.Entry<String, Integer> f : freqList) {
                namer.items.add(f.getKey());
            }
        } catch (IOException e) {
            Log.w("WaypointNamer", "Not able to read addresses:" + e.getMessage());
        }
        namer.lastSelection = 0;
        return namer;
    }

    private static void updateHash(Map<String, Integer> map, String key) {
        if (key == null) {
            return;
        }
        if (map.containsKey(key)) {
            map.put(key, map.get(key) + 1);
        } else {
            map.put(key, 1);
        }
    }

    private WaypointNamer(LatLng position) {
        this.position = position;
        items = Collections.EMPTY_LIST;
    }

    public String getNextName() {
        if (items.size() == 0) {
            return defaultName;
        }
        String result = items.get(lastSelection);
        lastSelection = lastSelection == items.size() - 1 ? 0 : lastSelection + 1;
        return result;
    }

    public List<String> getAllNames() {
        return new ArrayList<String>(items);
    }
}
