package com.gpsaviator.encoders.binary;

import com.gpsaviator.*;
import com.gpsaviator.encoders.Binariser;
import com.gpsaviator.CoordinateFactory;

import java.nio.ByteBuffer;

/**
 * Created by khaines on 05/12/2014.
 */
public class WaypointBinariser implements Binariser<Waypoint> {

    private CoordinateFactory cf;

    WaypointBinariser(CoordinateFactory cf) {
        this.cf = cf;
    }

    @Override
    public int byteSize(Waypoint wp) {
        return (2 * Double.SIZE/8) + 2 * Float.SIZE / 8 + Utils.byteStringLen(wp.getIdent())
                + wp.getInfo().byteSize(wp.getInfo());
    }

    @Override
    public Waypoint fromBuffer(ByteBuffer bb) {
        float altitude = bb.getFloat();
        float magVar = bb.getFloat();
        Coordinate coord = cf.create(bb.getDouble(), bb.getDouble());

        String ident = com.gpsaviator.Utils.getByteString(bb);
        InfoMap info = new InfoMap(bb);

        Waypoint wp = new Waypoint(coord, magVar, altitude, ident, null);
        wp.setInfo(info);
        return wp;
    }

    @Override
    public void toBuffer(Waypoint wp, ByteBuffer bb) {
        bb.putFloat(wp.getAltitude());
        bb.putFloat(wp.getMagVar());
        bb.putDouble(wp.getCoord().getLat());
        bb.putDouble(wp.getCoord().getLon());
        Utils.putByteString(bb, wp.getIdent());
        wp.getInfo().toBuffer(wp.getInfo(), bb);
    }
}
