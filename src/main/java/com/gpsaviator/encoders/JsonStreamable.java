package com.gpsaviator.encoders;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.json.JSONException;

public interface JsonStreamable {

	public void readStream(InputStream is) throws IOException, JSONException;

	public void writeStream(OutputStream os) throws IOException, JSONException;

}
