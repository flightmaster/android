package com.gpsaviator.encoders.binary;

import com.gpsaviator.*;
import com.gpsaviator.encoders.Binariser;
import com.gpsaviator.CoordinateFactory;

import java.nio.ByteBuffer;

/**
 * Created by khaines on 08/12/14.
 */
public class BoundaryBinariser implements Binariser<Boundary> {

    private static final byte _BYTE_ARC = 0;
    private static final byte _BYTE_CIRCLE = 1;
    private static final byte _BYTE_LINE = 2;
    private BoundaryLineBinariser lineBinariser;
    private BoundaryCircleBinariser circleBinariser;
    private BoundaryArcBinariser arcBinariser;

    public BoundaryBinariser(CoordinateFactory cf) {
        this.lineBinariser = new BoundaryLineBinariser(cf);
        this.circleBinariser = new BoundaryCircleBinariser(cf);
        this.arcBinariser = new BoundaryArcBinariser(cf);
    }

    @Override
    public int byteSize(Boundary b) {

        int size = 0;
        if (b instanceof BoundaryLine) {
            size = lineBinariser.byteSize((BoundaryLine) b);
        } else if (b instanceof BoundaryCircle) {
            size = circleBinariser.byteSize((BoundaryCircle) b);
        } else if (b instanceof BoundaryArc) {
            size = arcBinariser.byteSize((BoundaryArc) b);
        }
        return size + 1;
    }

    @Override
    public Boundary fromBuffer(ByteBuffer bb) {
        byte type = bb.get();
        switch (type) {
            case _BYTE_LINE:
                return lineBinariser.fromBuffer(bb);
            case _BYTE_CIRCLE:
                return circleBinariser.fromBuffer(bb);
            case _BYTE_ARC:
                return arcBinariser.fromBuffer(bb);
        }

        throw new RuntimeException(String.format("Unrecognised boundary type %d", (int) type));
    }

    @Override
    public void toBuffer(Boundary b, ByteBuffer bb) {
        if (b instanceof BoundaryLine) {
            bb.put(_BYTE_LINE);
            lineBinariser.toBuffer((BoundaryLine) b, bb);
        } else if (b instanceof BoundaryCircle) {
            bb.put(_BYTE_CIRCLE);
            circleBinariser.toBuffer((BoundaryCircle) b, bb);
        } else if (b instanceof BoundaryArc) {
            bb.put(_BYTE_ARC);
            arcBinariser.toBuffer((BoundaryArc) b, bb);
        }
    }
}
