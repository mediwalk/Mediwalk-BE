package com.example.mediwalk_be.domain.walk.util;

import java.util.List;

/**
 * Google Encoded Polyline Algorithm Format (lat, lng 순서로 인코딩).
 */
public final class PolylineEncoder {

	private PolylineEncoder() {
	}

	public static String encodeLatLngPairs(List<double[]> latLngPairs) {
		StringBuilder result = new StringBuilder();
		int prevLat = 0;
		int prevLng = 0;
		for (double[] pair : latLngPairs) {
			int lat = (int) Math.round(pair[0] * 1e5);
			int lng = (int) Math.round(pair[1] * 1e5);
			encodeSigned(lat - prevLat, result);
			encodeSigned(lng - prevLng, result);
			prevLat = lat;
			prevLng = lng;
		}
		return result.toString();
	}

	private static void encodeSigned(int v, StringBuilder out) {
		int sgn = v << 1;
		if (v < 0) {
			sgn = ~sgn;
		}
		encodeUnsigned(sgn, out);
	}

	private static void encodeUnsigned(int v, StringBuilder out) {
		while (v >= 0x20) {
			out.append((char) ((0x20 | (v & 0x1f)) + 63));
			v >>= 5;
		}
		out.append((char) (v + 63));
	}
}
