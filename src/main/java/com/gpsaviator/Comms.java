package com.gpsaviator;

public class Comms {
	private final int frequency; // frequency * 100 e.g. 118.90 is encoded as 11890
	private final String callsign;

	public Comms(int freq) {
		this.frequency = freq;
		this.callsign = null;
	}

	public Comms(int freq, String callsign) {
		this.frequency = freq;
		this.callsign = callsign;
	}

	public int getFrequency() {
		return frequency;
	}

	public String getCallsign() {
		return callsign;
	}

}