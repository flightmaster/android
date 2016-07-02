package com.gpsaviator.encoders.binary;

import com.gpsaviator.*;
import com.gpsaviator.encoders.Binariser;

import java.nio.ByteBuffer;

/**
 * Created by khaines on 09/12/14.
 */
public class AirspaceBinariser implements Binariser<Airspace> {

    private final BoundaryBinariser boundaryBinariser;

    public AirspaceBinariser(CoordinateFactory coordinateFactory) {
        boundaryBinariser = new BoundaryBinariser(coordinateFactory);
    }

    @Override
    public int byteSize(Airspace a) {
        int boundsSize = Integer.SIZE / 8;

        for (Boundary b : a.getBoundaries()) {
            boundsSize += boundaryBinariser.byteSize(b);
        }
        return Utils.byteStringLen(a.getUpper().toString())
                + Utils.byteStringLen(a.getLower().toString())
                + Utils.byteStringLen(a.getName()) + Utils.byteStringLen(a.getNotes())
                + Utils.byteStringLen(a.getAsClass().toString()) + boundsSize;
    }

    @Override
    public Airspace fromBuffer(ByteBuffer bb) {
        Altitude upper = new Altitude(Utils.getByteString(bb));
        Altitude lower = new Altitude(Utils.getByteString(bb));
        String name = Utils.getByteString(bb);
        String notes = Utils.getByteString(bb);
        Airspace.AsClass ac;
        try {
            ac = Airspace.AsClass.valueOf(Utils.getByteString(bb));
        } catch (Exception e) {
            ac = Airspace.AsClass.Other;
        }

        int numBounds = bb.getInt();
        Boundary[] boundaries = new Boundary[numBounds];
        for (int j = 0; j < numBounds; j++) {
            boundaries[j] = boundaryBinariser.fromBuffer(bb);
        }

        return new Airspace(ac, name, notes, boundaries, lower, upper);
    }

    @Override
    public void toBuffer(Airspace a, ByteBuffer bb) {
        Utils.putByteString(bb, a.getUpper().toString());
        Utils.putByteString(bb, a.getLower().toString());
        Utils.putByteString(bb, a.getName());
        Utils.putByteString(bb, a.getNotes());
        Utils.putByteString(bb, a.getAsClass().toString());

        bb.putInt(a.getBoundaries().length);
        for (Boundary b : a.getBoundaries()) {
            boundaryBinariser.toBuffer(b, bb);
        }

        // don't know why this doesn't work:
        // ArrayList<Boundary> lb = (ArrayList<Boundary>) Arrays.asList(bounds);
        // Utils.writeByteBuffer(bb, lb);
    }
}
