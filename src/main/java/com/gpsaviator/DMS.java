package com.gpsaviator;

// Class for representing coordinate using Degrees, Minutes and deci-secs
public class DMS {
	public static double rad2nm(double d) {
		return d * (10800 / Math.PI);
	}

    public static double nm2rad(double d) {
        return (d * Math.PI) / 10800;
    }

    private final int d;

	private final int m;

	private final int s;

	private final boolean positive;

	public DMS(double deg) {
		long degValue = Math.abs(Math.round(deg * 360000));
		int d = (int) (degValue / 360000);
		int m = (int) (degValue / 6000) % 60;
		int s = (int) (degValue % 6000);

		this.d = d;
		this.m = m;
		this.s = s;
		positive = (deg >= 0.0) ? true : false;
	}

	DMS(int d, int m, int s) {
		positive = (d >= 0);
		this.d = Math.abs(d);
		this.m = m;
		this.s = s;
	}

	public DMS(String str) {
		int offset = str.length() == 10 ? 4 : 3;
		d = Integer.valueOf(str.substring(1, offset));
		positive = !(str.charAt(0) == '-');
		m = Integer.valueOf(str.substring(offset, offset + 2));
		s = Integer.valueOf(str.substring(offset + 2, offset + 6));
	}

	public double toRad() {
		return Math.toRadians(toDeg());
	}

    public double toDeg() {
        double result = (d + ((double) m) / 60 + ((double) s) / 360000);
        return positive ? result : -result;
    }

	@Override
	public String toString() {
		return (positive ? "+" : "-") + String.format("%03d%02d%04d", d, m, s);
	}

	public String[] toStringArray() {
		String str[] = new String[3];
		str[0] = String.format("%03d", d);
		str[1] = String.format("%02d", m);
		str[2] = String.format("%05.2f", (float) s / 100);
		return str;
	}

}
