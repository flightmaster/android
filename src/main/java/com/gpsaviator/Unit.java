package com.gpsaviator;

/**
* Created by khaines on 26/12/2014.
*/
public enum Unit {
    RADIANS("rad"),
    DEGREES("deg"),
    NAUTICAL_MILES("nm"),
    MILES("m"),
    FEET("ft"),
    KILOMETRES("km"),
    METRES("m"),

    DAYS("days"),
    HOURS("hour"),
    MINUTES("min"),
    SECONDS("secs");

    private String desc;

    Unit(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
