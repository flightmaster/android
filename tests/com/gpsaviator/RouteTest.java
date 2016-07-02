package com.gpsaviator;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;
import static com.gpsaviator.Route.RouteWaypoint.*;

public class RouteTest {

    private static final int MIN_GRID = -20;
    private static final int MAX_GRID = 20;

    private static CoordinateFactory cf = CoordinateDefaultFactory.getInstance();
    private static GridWps grid;

    private static class GridWps {
        private final int width;
        private int min;
        private int max;
        private static Route.RouteWaypoint gridWps[];

        GridWps(int min, int max) {
            this.min = min;
            this.max = max;
            this.width = max-min;
            gridWps = new Route.RouteWaypoint[width*width];
            for (int i = min; i < max; i++) {
                for (int j = min; j < max; j++) {
                    gridWps[getEntryIdx(i, j)] = make(cf.create(i, j), i, String.format("%d-%d", i, j), "");
                }
            }
        }

        private int getEntryIdx(int x, int y) {
            return (x - min) + (y-min) * width;
        }

        private Route.RouteWaypoint getEntry(int x, int y) {
            return gridWps[getEntryIdx(x, y)];
        }
    }

    private Route mainRoute = null;

    @BeforeClass
    public static void init() {
        grid = new GridWps(MIN_GRID, MAX_GRID);
    }

    @Before
    public void setUp() throws Exception {
        Route.RouteWaypoint[] items = {
                grid.getEntry(0, 0),
                grid.getEntry(0, 1),
                grid.getEntry(1, 1),
                grid.getEntry(1, 2),
                grid.getEntry(2, 2),
                grid.getEntry(2, 3)};
        mainRoute = Route.create();
        mainRoute.addPoint(items);
    }

    @After
    public void tearDown() throws Exception {

    }

    /**
     * Load waypoints into a plan and make sure that the plan reflects the correct number
     * of legs and waypoints.
     */
    @Test
    public void testAddAndRemove() {
        Route r = Route.create();

        assertEquals(0, r.getNumLegs());
        assertEquals(0, r.getNumPoints());

        r.addPoint(grid.getEntry(0, 0));
        assertEquals(1, r.getNumPoints());
        assertEquals(0, r.getNumLegs());

        for (int j = 1; j < r.getMaxTurnPoints(); j++) {
            r.addPoint(grid.getEntry(j % MAX_GRID, j % MAX_GRID));
            assertEquals(j+1, r.getNumPoints());
            assertEquals(j, r.getNumLegs());
        }
        assertEquals(r.getMaxTurnPoints(), r.getNumPoints());
        r.addPoint(grid.getEntry(0, 0));
        assertEquals(r.getMaxTurnPoints(), r.getNumPoints());

        r.deletePoint(0);
        assertEquals(r.getMaxTurnPoints() - 1, r.getNumPoints());

        // try to delete a non-existent point
        r.deletePoint(r.getNumPoints());
        assertEquals(r.getMaxTurnPoints() - 1, r.getNumPoints());
        r.deletePoint(r.getNumPoints() - 1);
        assertEquals(r.getMaxTurnPoints() - 2, r.getNumPoints());

        for (int j = r.getNumPoints(); j > 0; j--) {
            r.deletePoint(0);
            assertEquals(j-1, r.getNumPoints());
        }
    }

    /**
     * Test that routes can be copied.
     */
    @Test
    public void testCopies() {
        Route r = mainRoute.copy();

        assertEquals(mainRoute.getNumPoints(), r.getNumPoints());
        int j = 0;
        for (Route.RouteWaypoint wp : mainRoute.getPoints()) {
            assertTrue(wp.equals(r.getPoint(j)));
            j++;
        }
    }

    private Route.RouteWaypoint getEnd(Route r, int offset) {
        return r.getPoint(r.getNumPoints() - offset - 1);
    }

    /**
     * Tests that the plan can be modified.
     */
    @Test
    public void testModifications() {

        Route r = mainRoute.copy();

        // Add a waypoint to the end   = 0, 1, 2, 3, 4, 5, 10
        final Route.RouteWaypoint entryToAdd = grid.getEntry(10, 10);
        r.addPoint(entryToAdd);
        assertEquals(entryToAdd, r.getEnd());

        // Remove the 2nd last waypoint  = 0, 1, 2, 3, 4, 10
        r.deletePoint(r.getNumPoints() - 2);
        assertEquals(entryToAdd, getEnd(r, 0));
        assertEquals(getEnd(mainRoute, 1), getEnd(r, 1));

        // Remove the first waypoint = 1,2,3,4,10
        r.deletePoint(0);
        assertEquals(mainRoute.getPoint(1), r.getPoint(0));

        // Remove the middle waypoint = 1,2,4,10
        r.deletePoint(2);
        assertEquals(mainRoute.getPoint(4), r.getPoint(2));

        // Add a new start waypoint = 11,1,2,4,10
        final Route.RouteWaypoint entryToAdd2 = grid.getEntry(11, 11);
        r.addPoint(entryToAdd2, 0);
        assertEquals(entryToAdd2, r.getPoint(0));
        assertEquals(mainRoute.getPoint(1), r.getPoint(1));
        assertEquals(mainRoute.getPoint(2), r.getPoint(2));
        assertEquals(mainRoute.getPoint(4), r.getPoint(3));
        assertEquals(entryToAdd, r.getPoint(4));
        System.out.println(r);
    }

    /**
     * Test that the legs functions return the correct leg structures
     */
    @Test
    public void testLegs() {
        Route r = mainRoute.copy();

        for (int j = 0; j < r.getNumLegs(); j++) {
            assertEquals(r.getPoint(j), r.getLeg(j).getStart());
            assertEquals(r.getPoint(j+1), r.getLeg(j).getEnd());
        }
    }
}