package com.gpsaviator.encoders.binary;

import com.gpsaviator.BoundaryArc;
import com.gpsaviator.encoders.Binariser;
import com.gpsaviator.Coordinate;
import com.gpsaviator.CoordinateFactory;

import java.nio.ByteBuffer;

/**
 * Created by khaines on 08/12/14.
 */
public class BoundaryArcBinariser implements Binariser<BoundaryArc> {

    private static final byte _BYTE_RIGHT = 0;
    private static final byte _BYTE_LEFT = 1;

    private CoordinateFactory cf;

    public BoundaryArcBinariser(CoordinateFactory cf) {
        this.cf = cf;
    }

    @Override
    public int byteSize(BoundaryArc arc) {
        return ( 6 * Double.SIZE / 8 + 1);  // 3 double pairs and l/r indicator
    }

    private Coordinate getCoord(ByteBuffer bb) {
        double lat = bb.getDouble();
        double lon = bb.getDouble();
        return cf.create(lat, lon);
    }

    @Override
    public BoundaryArc fromBuffer(ByteBuffer bb) {
        Coordinate start = getCoord(bb);
        Coordinate end = getCoord(bb);
        Coordinate centre = getCoord(bb);
        BoundaryArc.ArcDirection dir = bb.get() == _BYTE_LEFT ? BoundaryArc.ArcDirection.LEFT : BoundaryArc.ArcDirection.RIGHT;
        return new BoundaryArc(start, end, centre, dir);
    }

    @Override
    public void toBuffer(BoundaryArc arc, ByteBuffer bb) {
        bb.putDouble(arc.getStart().getLat());
        bb.putDouble(arc.getStart().getLon());
        bb.putDouble(arc.getEnd().getLat());
        bb.putDouble(arc.getEnd().getLon());
        bb.putDouble(arc.getCentre().getLat());
        bb.putDouble(arc.getCentre().getLon());
        bb.put(arc.getDir() == BoundaryArc.ArcDirection.LEFT ? _BYTE_LEFT : _BYTE_RIGHT);
    }
}
