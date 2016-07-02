package com.gpsaviator;

import lombok.Getter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by khaines on 26/11/2014.
 */
public class RouteDB {

    private static final String TAG = "ROUTE_DB";

    public static class RouteDBEntry {

        @Getter
        private String id;

        @Getter
        private Route route;

        private RouteDBEntry() {

        }

        /**
         * Create RouteDBEntry object, makes a copy of the route parameter.
         * @param id
         * @param route
         */
        public RouteDBEntry(String id, Route route) {
            if (route == null) {
                throw new IllegalArgumentException("Route cannot be null in RouteDBEntry");
            }
            this.id = id;
            this.route = route.copy();
        }

        public String getStartIdent() {
            return route.getStart().getIdent();
        }

        public String getEndIdent() {
            return route.getEnd().getIdent();
        }

    }

    private Map<String, Route> routeDB;

    public RouteDB() {
        routeDB = new HashMap<String, Route>();
    }

//    public void readStream(InputStream stream) throws IOException, JSONException {
//        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
//
//        String line;
//        Log.d(TAG, "JSON read started");
//
//        while (br.ready()) {
//            line = br.readLine();
//            RouteDBEntry r = RouteDBEntry.fromJSON(new JSONObject(line));
//            routeDB.put(r.id, r.route);
//        }
//    }
//
//    public void writeStream(OutputStream stream) throws JSONException, IOException {
//        for (String id : routeDB.keySet()) {
//            RouteDBEntry r = getRoute(id);
//            JSONObject json = r.toJSON();
//            stream.write(json.toString().getBytes());
//            stream.write(10);
//        }
//
//    }
//
    /**
     * Adds a copy of the specified route to the database. If the route is identical to
     * another route in the database then no action is taken.
     *
     *
     * @param route Route to add
     * @return RouteDBEntry describing the route just added.
     */
    public RouteDBEntry addRoute(Route route) {
        RouteDBEntry entry = findMatch(route);
        if (entry != null) {
            return entry;
        }
        String id = Integer.toHexString(this.hashCode());
        RouteDBEntry result = new RouteDBEntry(id, route);
        routeDB.put(id, result.getRoute());
        return result;
    }

    /**
     * Find the first matching route in the database.
     * @param route Route to search for.
     * @return Matching routeDBEntry or null if not found.
     */
    private RouteDBEntry findMatch(Route route) {
        for (String i : routeDB.keySet()) {
            Route r = routeDB.get(i);
            if (r.equals(route)) {
                return new RouteDBEntry(i, r);
            }
        }
        return null;
    }

    /**
     * Delete the route associated with the Id.
     *
     * @param id Throws IllegalArgumentException if the Id is invalid.
     */
    public void deleteRoute(String id) {
        validateRouteId(id);
        routeDB.remove(id);
    }

    /**
     * Check if the given route Id exists in the database.
     * @param id
     * @return true if so.
     */
    public boolean exists(String id) {
        return routeDB.containsKey(id);
    }

    /**
     * Update the given route with a new route.
     * @param rdbe Entry to update. This should be considered invalid following the update.
     * @return RouteDBEntry for the item just updated. The id may change to enforce
     * uniqueness of routes within the database so callers must use this result from then on.
     *
     * @throws java.lang.IllegalArgumentException if the id is invalid/not found/
     */
    public RouteDBEntry updateRoute(RouteDBEntry rdbe) {
        validateRouteId(rdbe.getId());
        RouteDBEntry entry = findMatch(rdbe.route);
        if (entry != null) {
            return entry;
        }
        RouteDBEntry r = new RouteDBEntry(rdbe.getId(), rdbe.getRoute());
        routeDB.put(r.id, r.getRoute());
        return rdbe;
    }

    /**
     * Get the route associated with the Id
     *
     * @param id Id of the route to get.
     * @return The route. Throws IllegalArgumentException if the Id is invalid.
     */
    public RouteDBEntry getRoute(String id) {
        validateRouteId(id);
        return new RouteDBEntry(id, routeDB.get(id));
    }

    public Collection<String> getRouteIds() {
        return routeDB.keySet();
    }


    private void validateRouteId(String id) {
        if (! routeDB.containsKey(id)) {
            throw new IllegalArgumentException(id + " not found in routeDB");
        }
    }

    /**
     * Return a list of RouteDBEntry objects for all routes that start
     * at the given ident.
     *
     * @param ident Ident that route must start at to be in results.
     * @return List of entries
     */
    public Collection<RouteDBEntry> getRoutesStartingAt(String ident) {

        return null;
    }

    /**
     * Return a list of RouteDBEntry objects for all routes that end
     * at the given ident.
     *
     * @param ident Ident that the route must end at to be in the results.
     * @return List of entries.
     */
    public Collection<RouteDBEntry> getRoutesEndingAt(String ident) {

        return null;
    }

    /**
     * Return a list of RouteDBEntry objects for all routes that run between
     * ident1 and ident2 in either direction.
     *
     * @param ident1 First ident to use.
     * @param ident2 Second ident to use.
     * @return List of entries.
     */
    public Collection<RouteDBEntry> getRoutesBetween(String ident1, String ident2) {

        return null;
    }
}
