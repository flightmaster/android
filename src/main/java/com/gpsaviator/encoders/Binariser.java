package com.gpsaviator.encoders;

import java.nio.ByteBuffer;

public interface Binariser<N> {

	/*
	 * compute the size in bytes needed to store the object in a ByteBuffer
	 */
	public int byteSize(N obj);

	/*
	 * recreate object from the specified ByteBuffer
	 */
	public N fromBuffer(ByteBuffer bb);

	/*
	 * Store binary representation of the object into the given ByteBuffer.
	 * Shouldn't be called directly, except via utility function
	 * Utils.asByteBuffer
	 * 
	 * An object should encode enough information to be able to recreate itself
	 * during its fromBuffer method.
	 */
	public void toBuffer(N object, ByteBuffer bb);

}
