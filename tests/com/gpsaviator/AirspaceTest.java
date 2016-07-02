package com.gpsaviator;

import com.gpsaviator.Altitude.AltType;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.*;

public class AirspaceTest {

	@Rule
	public final TemporaryFolder temp = new TemporaryFolder();

	private static final String TEST_NOTES = "Test Notes\nTest Test Test";
	private static final String TEST_SPACE = "Test Space";
	private static final Altitude testUpper = new Altitude(AltType.FLIGHTLEVEL, 250);
	private static final Altitude testLower = new Altitude(AltType.FLIGHTLEVEL, 100);

	private static final String json1 = "{\"upper\" : \"F195\", \"name\" : \"SONDRESTROM CTA\", "
			+ "\"lower\" : \"A6500\", \"bounds\" : [ {\"radius\" : 90, \"type\" : \"circle\", \"centre\" : { \"lat\" : \"+067001100\","
			+ "\"lon\" : \"-050403100\"} }], \"class\" : \"E\", "
			+ "\"notes\" :\"SONDRESTROM TWR/APP\\nWIN: MON-SAT 1000-2200, EXC HOL; SUM: MON-SAT 0900-2100,EXC HOL.\"}";

	private static final String json2 = "{\"upper\" : \"A3500\", \"name\" : \"BRIZE NORTON CTR\", \"lower\" : \"G0\", "
			+ "\"bounds\" : [ {\"type\" : \"line\", \"coord\" : {\"lat\" : \"+051500534\", \"lon\" : \"-001292860\"}},"
			+ " {\"type\" : \"arc\", \"dir\" : \"R\", \"start\" : {\"lat\" : \"+051395031\", \"lon\" : \"-001403485\"}, "
			+ "\"end\" : {\"lat\" : \"+051421792\", \"lon\" : \"-001442749\"}, \"centre\" : {\"lat\" : \"+051444300\", \"lon\" : \"-001363100\"}}, "
			+ "{\"type\" : \"line\", \"coord\" : {\"lat\" : \"+051482672\", \"lon\" : \"-001204138\"}}, {\"type\" : \"arc\", "
			+ "\"dir\" : \"R\", \"start\" : {\"lat\" : \"+051395031\", \"lon\" : \"-001403485\"}, \"end\" : {\"lat\" : "
			+ "\"+051421792\", \"lon\" : \"-001442749\"}, \"centre\" : {\"lat\" : \"+051444300\", \"lon\" : \"-001363100\"}},"
			+ " {\"type\" : \"line\", \"coord\" : {\"lat\" : \"+051434900\", \"lon\" : \"-001175300\"}}, {\"type\" : \"line\","
			+ " \"coord\" : {\"lat\" : \"+051395031\", \"lon\" : \"-001403485\"}}, {\"type\" : \"arc\", \"dir\" : \"R\", \"start\""
			+ " : {\"lat\" : \"+051395031\", \"lon\" : \"-001403485\"}, \"end\" : {\"lat\" : \"+051421792\", \"lon\" : \"-001442749\"},"
			+ " \"centre\" : {\"lat\" : \"+051444300\", \"lon\" : \"-001363100\"}}, {\"type\" : \"line\", \"coord\" : {\"lat\" :"
			+ " \"+051412000\", \"lon\" : \"-001500100\"}}, {\"type\" : \"line\", \"coord\" : {\"lat\" : \"+051455600\", \"lon\" "
			+ ": \"-001520200\"}}, {\"type\" : \"line\", \"coord\" : {\"lat\" : \"+051500534\", \"lon\" : \"-001292860\"}}], "
			+ "\"class\" : \"D\", \"notes\" : \"BRIZE ZONE/RADAR\\n\"}";

	public AirspaceTest() {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testConstructor() {

		Airspace a = new Airspace(Airspace.AsClass.A, TEST_SPACE, TEST_NOTES, null, testLower, testUpper);

		assertEquals(Airspace.AsClass.A, a.getAsClass());
		assertEquals(TEST_SPACE, a.getName());
		assertEquals(TEST_NOTES, a.getNotes());

		assertEquals(testLower, a.getLower());
		assertEquals(testUpper, a.getUpper());

	}

	@Test
	public void testJson() throws JSONException {

//		JSONObject j = new JSONObject(json1);
//
//		Airspace a = new Airspace(j);
//
//		assertEquals("FL195", a.getUpper().getAsString());
//		assertEquals("6500", a.getLower().getAsString());
//		assertEquals("SONDRESTROM CTA", a.getNotes());
//		assertEquals("SONDRESTROM TWR/APP\nWIN: MON-SAT 1000-2200, EXC HOL; SUM: MON-SAT 0900-2100,EXC HOL.",
//				a.getNotes());

	}

	@Test
	public void testJsonBounds() throws JSONException {
		JSONObject j = new JSONObject(json1);

//		Airspace a = new Airspace(j);
//		Coordinate coord = new Coordinate("+67001100", "-050403100");
//		double radius = 90;
//		BoundaryCircle c = new BoundaryCircle(coord, radius);
//
//		Boundary b = a.getBoundaries()[0];
//
//		assertNotNull(b);
//		assertEquals(new BoundaryCircle(null, 0).getClass().toString(), b.getClass().toString());
//
//		BoundaryCircle bc = (BoundaryCircle) b;
//		assertEquals(coord, bc.getCentre());
//		assertEquals(radius, bc.getRadius(), 0);
	}

	@Test
	public void testJsonAndBack() throws JSONException {

//		JSONObject j = new JSONObject(json1);
//		Airspace a = new Airspace(j);
//		JSONAssert.assertEquals(j.toString(), a.toJSON().toString(), false);
//
//		j = new JSONObject(json2);
//		a = new Airspace(j);
//		JSONAssert.assertEquals(j.toString(), a.toJSON().toString(), false);
	}

	@Test
	public void testByteableInteface() throws JSONException, Exception {
		JSONObject jarr[] = { new JSONObject(json1), new JSONObject(json2), };

//		for (JSONObject json : jarr) {
//			Airspace a = new Airspace(json);
//			int bufSize = a.byteSize();
//			assertTrue(bufSize > 0);
//
//			ByteBuffer bb = ByteBuffer.allocate(bufSize);
//			a.toBuffer(bb);
//
//			bb.rewind();
//
//			Airspace b = new Airspace(bb);
//			JSONAssert.assertEquals(json.toString(), b.toJSON().toString(), false);
//		}

	}
}
