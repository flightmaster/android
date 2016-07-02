package com.gpsaviator;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class Utils {

	static public int byteStringLen(String i) {
		return i.length() + Short.SIZE / 8;
	}

	static public ByteBuffer fromByteBuffer(ByteBuffer bb) {
		return bb;
	}

	static public String getByteString(ByteBuffer bb) {
		int len = (int) (bb.getShort());
		try {
			String result = new String(bb.array(), bb.position(), len, "UTF-8");
			bb.position(bb.position() + len);
			return result;
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	static public boolean loadByteBuffer(ByteBuffer bb, InputStream is) {
		try {
			// ByteBuffer bb = ByteBuffer.allocate(2048);
			is.read(bb.array(), 0, Integer.SIZE / 8);
			int size = bb.getInt();
			// bb = ByteBuffer.allocate(size);
			bb.rewind();
			is.read(bb.array(), 0, size);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	static public void putByteString(ByteBuffer bb, String i) {
		bb.putShort((short) i.getBytes().length);
		bb.put(i.getBytes());
	}

}
