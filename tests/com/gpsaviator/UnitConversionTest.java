package com.gpsaviator;

import org.junit.Test;

import static com.gpsaviator.Unit.*;
import static com.gpsaviator.UnitConversion.*;
import static junit.framework.TestCase.assertEquals;

public class UnitConversionTest {

    private static double ep = 1e-8;

    private void testBoth(Unit unit1, double value1, Unit unit2, double value2) {
        assertEquals(value2, convert(value1, unit1, unit2), ep);
        assertEquals(value1, convert(value2, unit2, unit1), ep);

        // repeat the test using a converter
        Converter cv = getConverter(unit1, unit2);
        assertEquals(value2, cv.convert(value1), ep);
        assertEquals(value1, cv.invert(value2), ep);


    }

    @Test
    public void testUnits() {
        testBoth(DEGREES, 180.0, RADIANS, Math.PI);
        testBoth(DEGREES, 1.0, NAUTICAL_MILES, 60.0);
        testBoth(NAUTICAL_MILES, 60*180, RADIANS, Math.PI);
        testBoth(DEGREES, 1.0, FEET, 60.0 * 6076.12);

        testBoth(NAUTICAL_MILES, 100, METRES, 100 * 1852);
        testBoth(NAUTICAL_MILES, 100, FEET, 607612);

        testBoth(HOURS, 1, SECONDS, 3600.0);

        testTime(NAUTICAL_MILES, HOURS, 100, NAUTICAL_MILES, HOURS, 100);
        testTime(NAUTICAL_MILES, HOURS, 100, NAUTICAL_MILES, MINUTES, 100 / 60.0);
        testTime(NAUTICAL_MILES, HOURS, 100, NAUTICAL_MILES, SECONDS, 100 / 3600.0);

        testTime(NAUTICAL_MILES, HOURS, 100, KILOMETRES, SECONDS, 0.05144444444444);
        testTime(NAUTICAL_MILES, HOURS, 100, FEET, SECONDS, 168.78111111111111);
        testTime(METRES, SECONDS, 100, NAUTICAL_MILES, HOURS, 194.3844492440604);
    }

    private void testTime(Unit unit1, Unit time1, double value1, Unit unit2, Unit time2, double value2) {
        assertEquals(value2, convert(value1, unit1, time1, unit2, time2), ep);
        assertEquals(value1, convert(value2, unit2, time2, unit1, time1), ep);

        // test a time-converter
        Converter cv = getConverter(unit1, time1, unit2, time2);
        assertEquals(value2, cv.convert(value1), ep);
        assertEquals(value1, cv.invert(value2), ep);
    }

}