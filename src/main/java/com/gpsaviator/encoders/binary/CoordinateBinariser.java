package com.gpsaviator.encoders.binary;

import com.gpsaviator.encoders.Binariser;
import com.gpsaviator.Coordinate;
import com.gpsaviator.CoordinateFactory;

import java.nio.ByteBuffer;

/**
 * Created by khaines on 06/12/2014.
 */
public class CoordinateBinariser implements Binariser<Coordinate> {

    private CoordinateFactory cf;

    public CoordinateBinariser(CoordinateFactory cf) {
        this.cf = cf;
    }

    @Override
    public int byteSize(Coordinate obj) {
        return (2 * Double.SIZE / 8);
    }

    @Override
    public Coordinate fromBuffer(ByteBuffer bb) {
        double lat = bb.getDouble();
        double lon = bb.getDouble();
        return cf.create(lat, lon);
    }

    @Override
    public void toBuffer(Coordinate c, ByteBuffer bb) {
        bb.putDouble(c.getLat());
        bb.putDouble(c.getLon());
    }
}
