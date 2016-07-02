package com.gpsaviator.android;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.IllegalFormatException;

import android.os.Environment;
import com.gpsaviator.encoders.JsonStreamable;
import com.gpsaviator.encoders.Jsoniser;
import org.json.JSONException;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import com.gpsaviator.encoders.Binariser;
import org.json.JSONObject;

public class Storage<T> {

    private static final String TAG = "Storage";

    public enum StorageType {
		EXTERNAL, INTERNAL,
	}

	public static void exportToSD(AviatorApp app, JSONObject obj, StorageType sp, String fileName) {
		FileOutputStream fos;

		try {
			fos = createOutputStream(app, sp, fileName);
			fos.write(obj.toString().getBytes());
			fos.write(10);
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static JSONObject importFromSD(AviatorApp app, StorageType sp, String fileName) {

		FileInputStream in;
		try {
			in = createInputStream(app, sp, fileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			Log.d(TAG, "read started");
			if (br.ready()) {
				String line = br.readLine();
				return new JSONObject(line);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO: Better exception
		throw new RuntimeException("Error parsing JSON");
	}

	public static void importFromSD(AviatorApp app, JsonStreamable destObj, int resourceID) {
		AssetFileDescriptor afd = app.getResources().openRawResourceFd(resourceID);

		try {
			destObj.readStream(afd.createInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public T readBinary(AviatorApp app, int resourceID, Binariser<T> b) {
        InputStream is = app.getResources().openRawResource(resourceID);
        return readBinary(app, is, 4096*1024, b);
    }

	public T readBinary(AviatorApp app, String fileName, Binariser<T> b) {
		try {
			File f = new File(app.getFilesDir(), fileName);
			Log.d(TAG, app.getFilesDir().getAbsolutePath());
			int size = (int) f.length();
			FileInputStream fis = new FileInputStream(f);
			return readBinary(app, fis, size, b);
		} catch (IOException e) {
			// TODO auto-generated catch block
			Log.d(TAG, "File binary read failed " + fileName);
			e.printStackTrace();
			return null;
		}
	}

	private T readBinary(AviatorApp app, InputStream fis, int size, Binariser<T> b) {
		Log.d(TAG, "Binary read started");
		try {
			ByteBuffer bb = ByteBuffer.allocate(size);
			fis.read(bb.array());
			T obj = b.fromBuffer(bb);
			fis.close();
			Log.d(TAG, "Binary read completed");
			return obj;
		} catch (IOException e) {
			// TODO auto-generated catch block
			e.printStackTrace();
			Log.d(TAG, "FileInputStream binary read failed");
			return null;
		}
	}

	public boolean writeBinary(AviatorApp app, String fileName, T obj, Binariser<T> b) {
		try {
			FileOutputStream fos = createOutputStream(app, StorageType.INTERNAL, fileName);
			int size = b.byteSize(obj);
			Log.d(TAG, Integer.toString(size));
			ByteBuffer bb = ByteBuffer.allocate(size);
			b.toBuffer(obj, bb);
			fos.write(bb.array());
			return true;
		} catch (IOException e) {
			// TODO auto-generated catch
			e.printStackTrace();
			return false;
		}
	}

	private static FileInputStream createInputStream(AviatorApp app, StorageType sp, String fileName)
			throws FileNotFoundException {
		FileInputStream in;
		if (sp == StorageType.INTERNAL) {
			in = app.openFileInput(fileName);
		} else {
			File externalFilesDir;
			File file;

			externalFilesDir = app.getExternalFilesDir(null);
			file = new File(externalFilesDir, fileName);
			in = new FileInputStream(file);
		}
		return in;
	}

	private static FileOutputStream createOutputStream(AviatorApp app, StorageType sp, String fileName)
			throws FileNotFoundException {
		File outFile;
		FileOutputStream fos;
		if (sp == StorageType.INTERNAL) {
            Log.d(TAG, app.getApplicationContext().getFilesDir().toString());
            fos = app.openFileOutput(fileName, Context.MODE_PRIVATE);
		} else {
            Log.d(TAG, app.getApplicationContext().getExternalFilesDir(null).toString());
            Log.d(TAG, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString());
            File externalFilesDir = app.getApplicationContext().getExternalFilesDir(null);
			outFile = new File(externalFilesDir, fileName);
			fos = new FileOutputStream(outFile);
		}
		return fos;
	}

	public static void importFromResource(AviatorApp app, JsonStreamable dest, int resourceId) {
		InputStream is = app.getResources().openRawResource(resourceId);
		try {
			dest.readStream(is);
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
