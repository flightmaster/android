package com.gpsaviator;

import com.gpsaviator.encoders.Binariser;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.util.Collection;

public class ByteableTest {

	public static void testByteableInterface(Collection<Binariser> bs) {

		int byteSize = 0;

//		for (Binariser b : bs) {
//			byteSize += b.byteSize();
//		}
//
//		ByteBuffer bb = ByteBuffer.allocate(byteSize);
//
//		for (Binariser b : bs) {
//			b.toBuffer(bb);
//		}
//
//		bb.rewind();
//
//		for (Binariser b : bs) {
//			Binariser b1 = b.fromBuffer(bb);
//			assertEquals(b, b1);
//		}
	}

}
