package com.gpsaviator;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gpsaviator.Altitude.AltType;

public class AltitudeTest {

	public AltitudeTest() {
	}

	@After
	public void clearDown() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testAltitudeAltTypeInt() {
		Altitude a = new Altitude(AltType.FLIGHTLEVEL, 350);
		testAlt(a, AltType.FLIGHTLEVEL, 35000, "FL350");

		Altitude b = new Altitude(AltType.ALTITUDE, 3500);
		testAlt(b, AltType.ALTITUDE, 3500, "3500");

		Altitude c = new Altitude(AltType.ABOVEGROUND, 3500);
		testAlt(c, AltType.ABOVEGROUND, 3500, "3500AGL");

	}

	@Test
	public void testAltitudeString() {
		Altitude a = new Altitude("F350");
		testAlt(a, AltType.FLIGHTLEVEL, 35000, "FL350");

		Altitude b = new Altitude("A3500");
		testAlt(b, AltType.ALTITUDE, 3500, "3500");

		Altitude c = new Altitude("G3500");
		testAlt(c, AltType.ABOVEGROUND, 3500, "3500AGL");

		// default when leading char is wrong is to assume ALTITUDE
		Altitude d = new Altitude("X3500");
		testAlt(d, AltType.ALTITUDE, 3500, "3500");

	}

	@Test(expected = NumberFormatException.class)
	public void testBadNumber() {
		Altitude a = new Altitude("XYZ");
	}

	@Test
	public void testByteInterface() {

		Altitude alts[] = { new Altitude("F350"), new Altitude("G1234"), new Altitude("A5500") };

		ByteableTest.testByteableInterface(new ArrayList(Arrays.asList(alts)));
	}

	private void testAlt(Altitude a, AltType type, int alt, String asString) {
		assertEquals(alt, a.getAlt());
		assertEquals(type, a.getType());
		assertEquals(asString, a.getAsString());
	}

}
