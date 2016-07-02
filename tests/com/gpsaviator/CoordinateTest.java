package com.gpsaviator;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class CoordinateTest {

    private static final int MIN_GRID = -90;
    private static final int MAX_GRID = 90;

    // TODO: * CoordinateFactory should be a parameter to the test for more flexibility
    private static CoordinateFactory cf = CoordinateDefaultFactory.getInstance();

    @Test
    public void testFundamentals() {
        Coordinate c = cf.create(51.5, -2);
        assertEquals(51.5, c.getLat());
        assertEquals(-2.0, c.getLon());
    }

    private double ep = 2 * 4.789e-8;   // 2 feet of error at the equator

    @Test
    public void testEquatorRange() {
        assertEquals(Math.toRadians(1), cf.create(0, 0).rangeTo(cf.create(1, 0)), ep);
        assertEquals(Math.toRadians(1), cf.create(0, 0).rangeTo(cf.create(-1, 0)), ep);
        assertEquals(Math.toRadians(1), cf.create(0, 0).rangeTo(cf.create(0, 1)), ep);
        assertEquals(Math.toRadians(1), cf.create(0, 0).rangeTo(cf.create(0, -1)), ep);
    }

    private double rangeBetween(int lat1, int lon1, int lat2, int lon2) {
        return cf.create(lat1, lon1).rangeTo(cf.create(lat2, lon2), Unit.KILOMETRES);
    }

    private void rangeCheckWithAccuracy(int lat1, int lon1, int lat2, int lon2, double distance) {
        // accuracy of distance functions is not usually better than 0.5% of the distance.
        assertEquals(distance, rangeBetween(lat1, lon1, lat2, lon2), (distance * 0.005));
    }

    @Test
    public void testNorthernRange() {
        UnitConversion.Converter cv = UnitConversion.getConverter(Unit.DEGREES, Unit.KILOMETRES);
        rangeCheckWithAccuracy(60, 0, 62, 0, cv.convert(2));
        rangeCheckWithAccuracy(60, 0, 60, 1, cv.convert(0.5));
        rangeCheckWithAccuracy(60, 1, 60, -1, cv.convert(1));

        // http://www.gpsvisualizer.com/calculators
        rangeCheckWithAccuracy(60, 1, 50, -3, 1141.748);
        rangeCheckWithAccuracy(30, -30, 55, 55, 7027.477);
        rangeCheckWithAccuracy(50, -170, 60, 170, 1684.22);
    }
}