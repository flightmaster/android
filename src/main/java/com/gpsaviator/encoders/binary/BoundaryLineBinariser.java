package com.gpsaviator.encoders.binary;

import com.gpsaviator.BoundaryLine;
import com.gpsaviator.CoordinateFactory;
import com.gpsaviator.encoders.Binariser;

import java.nio.ByteBuffer;

/**
 * Created by khaines on 08/12/14.
 */
public class BoundaryLineBinariser implements Binariser<BoundaryLine> {

    CoordinateFactory cf;

    BoundaryLineBinariser(CoordinateFactory cf) {
        this.cf = cf;
    }

    @Override
    public int byteSize(BoundaryLine obj) {
        return (2 * Double.SIZE / 8);
    }

    @Override
    public BoundaryLine fromBuffer(ByteBuffer bb) {
        double lat = bb.getDouble();
        double lon = bb.getDouble();
        return new BoundaryLine(cf.create(lat, lon));
    }

    @Override
    public void toBuffer(BoundaryLine bl, ByteBuffer bb) {
        bb.putDouble(bl.getEnd().getLat());
        bb.putDouble(bl.getEnd().getLon());
    }
}
