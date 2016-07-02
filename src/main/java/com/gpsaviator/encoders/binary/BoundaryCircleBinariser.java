package com.gpsaviator.encoders.binary;

import com.gpsaviator.BoundaryCircle;
import com.gpsaviator.encoders.Binariser;
import com.gpsaviator.CoordinateFactory;

import java.nio.ByteBuffer;

/**
 * Created by khaines on 08/12/14.
 */
public class BoundaryCircleBinariser implements Binariser<BoundaryCircle> {
    private final CoordinateFactory cf;

    public BoundaryCircleBinariser(CoordinateFactory cf) {
        this.cf = cf;
    }

    @Override
    public int byteSize(BoundaryCircle obj) {
        return 3 * Double.SIZE / 8;   // Centre coord, and radius
    }

    @Override
    public BoundaryCircle fromBuffer(ByteBuffer bb) {
        double lat = bb.getDouble();
        double lon = bb.getDouble();
        double radius = bb.getDouble();
        return new BoundaryCircle(cf.create(lat, lon), radius);
    }

    @Override
    public void toBuffer(BoundaryCircle bc, ByteBuffer bb) {
        bb.putDouble(bc.getCentre().getLat());
        bb.putDouble(bc.getCentre().getLon());
        bb.putDouble(bc.getRadius());
    }
}
