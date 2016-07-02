package com.gpsaviator;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;

import com.gpsaviator.encoders.Binariser;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * The InfoMap class allows waypoints to store key/value pairs for extra
 * type-specific information
 */

// TODO: ** Implement InfoMapBinariser
// TODO: ** Implement Jsoniser

public class InfoMap extends HashMap<String, String> implements Binariser<InfoMap> {

	public enum Types {
		AIRPORT, NDB, VOR, VORDME, VORTAC, WAYPOINT

	}

	public static final String _NAME = "name";

	private static final String _TYPE = "type";
	public static final String _AIRPORT = Types.AIRPORT.toString();
	public static final String _NDB = "NDB";
	public static final String _VOR = "VOR";
	public static final String _VORDME = "VOR/DME";
	public static final String _VORTAC = "VORTAC";
	public static final String _WAYPOINT = "WAYPOINT";

	public InfoMap() {
		super();
	}

    public InfoMap(InfoMap infoMap) {
        super(infoMap);
    }

    public InfoMap(ByteBuffer bb) {
		super();
		fromBuffer(bb);
	}

	public InfoMap(JSONObject json) throws JSONException {
		super();
		if (json == null) {
			return;
		}

		Iterator<String> i = json.keys();

		while (i.hasNext()) {
			String index = i.next();
			put(index.intern(), json.getString(index).intern());
		}
	}

	@Override
	public int byteSize(InfoMap im) {
		int total = Short.SIZE / 8; // include short header for 'size' (num of
									// elements)
		for (String i : keySet()) {
			total += Utils.byteStringLen(i) + Utils.byteStringLen(get(i));
		}
		return total;
	}

	@Override
	public InfoMap fromBuffer(ByteBuffer bb) {
		int num = bb.getShort();
		for (int j = 0; j < num; j++) {
			String key = Utils.getByteString(bb);
			String val = Utils.getByteString(bb);
			set(key.intern(), val.intern());
		}
		return this;
	}

	public double getDouble(String key) {
		return Double.parseDouble(get(key));
	}

	public int getInt(String key) {
		return Integer.parseInt(get(key));
	}

	public String getName() {
		return get(_NAME);
	}

	public String getString(String key) {
		return get(key);
	}

	public String getType() {
		return get(_TYPE);
	}

	public void set(String key, String value) {
		put(key.intern(), value.intern());
	}

	@Override
	public void toBuffer(InfoMap im, ByteBuffer bb) {
		bb.putShort((short) im.size());
		for (String i : im.keySet()) {
			Utils.putByteString(bb, i);
			Utils.putByteString(bb, im.get(i));
		}

	}

	public JSONObject toJSON() throws JSONException {

		JSONObject json = new JSONObject();

		for (String i : keySet()) {
			json.put(i, get(i));
		}

		return json;

	}
}
