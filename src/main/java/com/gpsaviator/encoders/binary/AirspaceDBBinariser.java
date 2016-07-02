package com.gpsaviator.encoders.binary;

import android.util.Log;
import com.gpsaviator.Airspace;
import com.gpsaviator.AirspaceDB;
import com.gpsaviator.CoordinateFactory;
import com.gpsaviator.encoders.Binariser;

import java.nio.ByteBuffer;

/**
 * Created by khaines on 09/12/14.
 */
public class AirspaceDBBinariser implements Binariser<AirspaceDB> {

    private static final String TAG = "ASDBBinariser";
    private final AirspaceBinariser ab;

    public AirspaceDBBinariser(CoordinateFactory cf) {
        this.ab = new AirspaceBinariser(cf);
    }

    @Override
    public int byteSize(AirspaceDB db) {
        int size = Integer.SIZE / 8;
        for (Airspace a : db.getAirspace()) {
            size += ab.byteSize(a);
        }
        return size;
    }

    @Override
    public AirspaceDB fromBuffer(ByteBuffer bb) {
        int count = bb.getInt();
        AirspaceDB db = new AirspaceDB(count);
        Log.d(TAG, "Loading " + count +" airspaces");
        try {
            for (int j = 0; j < count; j++) {
                db.add(ab.fromBuffer(bb));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        Log.d(TAG, "Completed");
        return db;
    }

    @Override
    public void toBuffer(AirspaceDB db, ByteBuffer bb) {
        bb.putInt(db.getAirspace().size());
        for (Airspace a : db.getAirspace()) {
            ab.toBuffer(a, bb);
        }
    }

}
