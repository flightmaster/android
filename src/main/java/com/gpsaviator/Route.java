package com.gpsaviator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import android.util.Pair;

public class Route implements Copy<Route> {

    /**
     * RouteWaypoints represent elements of a route. They are immutable and so can be
     * shared freely amongst routes.
     */
    public static class RouteWaypoint {

        @Getter
        private String ident;

        @Getter
        private String notes;

        @Getter
        private Coordinate coord;

        @Getter
        private float magVar;

        private RouteWaypoint(Coordinate coord, float magVar, String ident, String name) {
            this.coord = coord;
            this.ident = ident;
            this.notes = name;
            this.magVar = magVar;
        }

        private RouteWaypoint(RouteWaypoint point) {
            this.coord = point.getCoord();
            this.ident = point.getIdent();
            this.notes = this.getNotes();
            this.magVar = point.getMagVar();
        }

        public static RouteWaypoint make(Coordinate coord, float magVar, String ident, String name) {
            return new RouteWaypoint(coord, magVar, ident, name);
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(19, 21).append(this.getIdent()).append(this.getNotes())
                    .append(this.getCoord()).build();
        }

        @Override
        public boolean equals(Object o) {
            RouteWaypoint oo = (RouteWaypoint) o;
            if (oo == this) return true;
            return oo.hashCode() == this.hashCode();
        }

        @Override
        public String toString() {
            return String.format("%s %f %f", getIdent(), getCoord().getLat(), getCoord().getLon());
        }
    }

	public class Leg {

        @Getter
        private RouteWaypoint start;

        @Getter
        private RouteWaypoint end;

        @Getter
        private double distanceFromPreviousLegs;

		public Leg(RouteWaypoint start, RouteWaypoint end, double distanceFromPreviousLegs) {
            this.distanceFromPreviousLegs = distanceFromPreviousLegs;
            this.start = start;
            this.end = end;
		}

		public double getCourse() {
			return getStart().getCoord().bearingTo(getEnd().getCoord());
		}

        public double getMagCourse() {
            final double mag = getCourse() + getStart().getMagVar();
            if (mag < 0) return mag + 2*Math.PI;
                else if (mag > 2*Math.PI) return mag - 2*Math.PI;
                else return mag;
        }

		public double getRange() {
			return getStart().getCoord().rangeTo(getEnd().getCoord());
		}

		@Override
		public String toString() {
			return String.format("%s-%s", start.getIdent(), end.getIdent());
		}
	}

    private static final int MAX_TURN_POINTS = 32;
	private ArrayList<RouteWaypoint> routeWaypoints;

    private Integer activeLeg = null;

	private Route() {
		super();
		routeWaypoints = new ArrayList<RouteWaypoint>(MAX_TURN_POINTS);
	}

    public static Route create() {
        return new Route();
    }

    public Route copy() {
        Route r = new Route();
        for (RouteWaypoint p : this.getPoints()) {
            r.addPoint(p);
        }
        return r;
    }

    public int getMaxTurnPoints() {
        return MAX_TURN_POINTS;
    }

    /**
     * Add point to the route. A copy of the point is made.
     * @param newwp
     */
	public void addPoint(RouteWaypoint... newwp) {
        for (RouteWaypoint r : newwp) {
            if (getNumPoints() < MAX_TURN_POINTS) {
                routeWaypoints.add(r);
            }
        }
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hb = new HashCodeBuilder(19,21);
        for (RouteWaypoint wp : getPoints()) {
            hb.append(wp);
        }
        return hb.build();
    }

    public void addPoint(RouteWaypoint wp, int pos) {
		if (pos <= getNumPoints()) {
			routeWaypoints.add(pos, wp);
            if (isActive() && pos <= activeLeg) activeLeg++;
		}
	}

	public void updatePoint(RouteWaypoint wp, int pos) {
		if (pos < routeWaypoints.size()) {
			routeWaypoints.set(pos, wp);
		}
	}

    public void movePoint(Coordinate c, int pos) {
        if (pos < routeWaypoints.size()) {
            RouteWaypoint wp = routeWaypoints.get(pos);
            routeWaypoints.set(pos, RouteWaypoint.make(c, wp.getMagVar(), wp.getIdent(), wp.getNotes()));
        }
    }

	public void deleteAll() {
		routeWaypoints.clear();
        clearActiveLeg();
	}

	public void deletePoint(int id) {
		if (id < routeWaypoints.size()) {
			routeWaypoints.remove(id);
            if (isActive()) {
                if (getNumLegs() == 0) {
                    clearActiveLeg();
                } else if (getActiveLeg() > getNumLegs() - 1) {
                    setActiveLeg(getNumLegs() - 1);
                }
            }
        }
	}

    public void reverse() {
        Collections.reverse(routeWaypoints);
        if (isActive()) activeLeg = (getNumLegs() - activeLeg) - 1;
    }

    public Leg getLeg(int num) {
        return getLegs().get(num);
    }

	public ArrayList<Leg> getLegs() {
		ArrayList<Leg> legs = new ArrayList<Leg>(getNumLegs());

        double totalDistance = 0.0;
		for (int j = 0; j < getNumLegs(); j++) {
            Leg leg = new Leg(routeWaypoints.get(j), routeWaypoints.get(j + 1), totalDistance);
            totalDistance += leg.getRange();
            legs.add(leg);
        }
        return legs;
	}

	public int getNumLegs() {
		return getNumPoints() > 0 ? getNumPoints() - 1 : 0;
	}

	public int getNumPoints() {
		return routeWaypoints.size();
	}

	public RouteWaypoint getPoint(int number) {
		if (number < getNumPoints()) {
			return routeWaypoints.get(number);
		}
		return null;
	}

	public List<RouteWaypoint> getPoints() {
		return new ArrayList<RouteWaypoint>(routeWaypoints);
	}

    /**
     * Gets the distance, in radians, between all points in the plan.
     * @return
     */
    public double getRouteDistance() {
        double distance = 0;
        for (int i = 1; i < getNumPoints(); i++ ) {
            distance += routeWaypoints.get(i).getCoord().rangeTo(routeWaypoints.get(i - 1).getCoord());
        }
        return distance;
    }

    /**
     * Works out the optimum leg at which to insert the given coordinate into the plan.
     *
     * @param crd Coordinate to work out
     * @return Index of place to add turnpoint (can be used as a parameter to addPoint)
     */
    public int getOptimumInsertPoint(Coordinate crd) {
        double extraDistance = Math.PI;
        int result = 0;
        for (int i = 1; i < getNumPoints(); i++) {
            final Coordinate startCoord = routeWaypoints.get(i).getCoord();
            final Coordinate endCoord = routeWaypoints.get(i - 1).getCoord();
            final double legDistance = startCoord.rangeTo(endCoord);
            final double extraLegDistance = startCoord.rangeTo(crd) + crd.rangeTo(endCoord) - legDistance;
            if (extraLegDistance < extraDistance) {
                result = i;
                extraDistance = extraLegDistance;
            }
        }
        return result;
    }

    /**
     * Get the first waypoint.
     * @return Waypoint
     */
    public RouteWaypoint getStart() {
        return routeWaypoints.get(0);
    }

    /**
     * Get the last waypoint of the route.
     * @return Last waypoint.
     */
    public RouteWaypoint getEnd() {
        return routeWaypoints.get(getNumPoints() - 1);
    }

    @Override
    public String toString() {
        return String.format("Route from %s to %s: %s", getStart().getIdent(), getEnd().getIdent(), getPoints());
    }

    public boolean isActive() {
        return activeLeg != null;
    }

    public Integer getActiveLeg() {
        return activeLeg;
    }

    public void setActiveLeg(int leg) {
        if (leg < 0 || leg >= getNumLegs()) throw new IllegalArgumentException("Leg does not exist");
        activeLeg = leg;
    }

    public void clearActiveLeg() {
        activeLeg = null;
    }
}
