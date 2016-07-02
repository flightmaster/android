package com.gpsaviator;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static com.gpsaviator.Unit.*;

/**
 * Created by khaines on 26/12/2014.
 */
public class UnitConversion {

    /**
     * Enforced non-instantiated object.
     */
    private UnitConversion() {
    }

    public static class Converter {
        private final double factor;

        private Converter(Unit from, Unit to) {
            factor = UnitConversion.convert(1, from, to);
        }

        public Converter(Unit from1, Unit from2, Unit to1, Unit to2) {
            factor = UnitConversion.convert(1.0, from1, from2, to1, to2);
        }

        public double convert(double val) { return val * factor; }

        public double invert(double val) { return val / factor; }
    }

    public static Converter getConverter(Unit from, Unit to) {
        return new Converter(from, to);
    }

    public static Converter getConverter(Unit from, Unit time1, Unit to, Unit time2) {
        return new Converter(from, time1, to, time2);
    }

    private static Map<Unit, Map<Unit, Double>> conversions;

    private static void putBoth(Unit from, Unit to, Double fromToFactor) {
        conversions.get(from).put(to, fromToFactor);
        conversions.get(to).put(from, 1 / fromToFactor);
        conversions.get(from).put(from, 1.0);
        conversions.get(to).put(to, 1.0);
    }

    static {
        conversions=new EnumMap<Unit, Map<Unit, Double>>(Unit.class);
        for (Unit u : values()) {
            conversions.put(u, new EnumMap<Unit, Double>(Unit.class));
        }
        putBoth(RADIANS, DEGREES, Math.toDegrees(1.0));
        putBoth(RADIANS, NAUTICAL_MILES, 60 * Math.toDegrees(1.0));
        putBoth(RADIANS, KILOMETRES, 60 * Math.toDegrees(1.0) * 1.852);
        putBoth(DEGREES, NAUTICAL_MILES, 60.0);
        putBoth(NAUTICAL_MILES, FEET, 6076.12);
        putBoth(NAUTICAL_MILES, KILOMETRES, 1.852);
        putBoth(NAUTICAL_MILES, METRES, 1852.0);
        putBoth(METRES, FEET, 3.2808399);

        putBoth(DAYS, HOURS, 24.0);
        putBoth(HOURS, MINUTES, 60.0);
        putBoth(MINUTES, SECONDS, 60.0);
        putBoth(HOURS, SECONDS, 3600.0);
    }

    private static Double findConversion(Unit from, Unit to) {
        final Map<Unit, Double> toMappings = conversions.get(from);
        if (toMappings != null) {
            final Double factor = toMappings.get(to);
            if (factor != null) {
                return factor;
            }
            //
            /**
             * There is no direct mapping, see what mappings we do have and try find
             * an indirect conversion.
             *
             * F -> T, doesn't work...
             * F -> V -> T might work if we can find a suitable V...
             *
             * Assumptions is: if we have F -> T in the mappings, we also have T -> F
             * Therefore we are looking for V common in F -> V and T -> V.
             *
             * So: Find all mappings from F, into [S1]:   F -> [S1]
             *     Find all mappings from T, into [S2]:   T -> [S2]
             *     Then intersect S1 & S2, and pick any item of the intersection!
             */

            System.out.print(String.format("Looking for intermediary: %s -> %s: ", from.toString(), to.toString()));
            if (conversions.get(to) != null) {
                Set<Unit> s1 = EnumSet.copyOf(toMappings.keySet());
                Set<Unit> s2 = conversions.get(to).keySet();
                s1.retainAll(s2);
                if (s1.size() > 0) {
                    Unit via = (Unit) (s1.toArray())[0];
                    System.out.println(via);
                    return findConversion(from, via) * findConversion(via, to);
                }
            }
        }
        return null;
    }

    public static double convert(double value, Unit from, Unit to) {

        final Double conversion = findConversion(from, to);
        if (conversion != null) {
            return value * conversion;
        }

        throw new UnsupportedOperationException(
                String.format("Conversion (%s to %s) not supported",
                        from.toString(), to.toString()));
    }

    public static double convert(double value, Unit from, Unit fromTime, Unit to, Unit toTime) {
        final Double conv1 = findConversion(from, to);
        if (conv1 == null) {
            throw new UnsupportedOperationException(
                    String.format("Conversion (%s to %s) not supported",
                            fromTime.toString(), toTime.toString()));
        }
        final Double conv2 = findConversion(fromTime, toTime);
        if (conv2 == null) {
            throw new UnsupportedOperationException(
                    String.format("Conversion (%s to %s) not supported",
                            from.toString(), to.toString()));

        }
        return value * conv1 / conv2;
    }
}
