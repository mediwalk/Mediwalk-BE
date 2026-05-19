package com.example.mediwalk_be.domain.walk.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Google Encoded Polyline Algorithm decode (각 점: 위도·경도 순).
 */
public final class PolylineDecoder {

	private PolylineDecoder() {
	}

	public static List<double[]> decodeToLatLonPairs(String encoded) {
		if (encoded == null || encoded.isBlank()) {
			return List.of();
		}
		List<double[]> coords = new ArrayList<>();
		int index = 0;
		int latE5 = 0;
		int lngE5 = 0;

		while (index < encoded.length()) {
			int[] res = decodeNextDelta(encoded, index);
			latE5 += res[0];
			index = res[1];

			res = decodeNextDelta(encoded, index);
			lngE5 += res[0];
			index = res[1];

			coords.add(new double[] {
					latE5 / 100_000.0,
					lngE5 / 100_000.0
			});
		}

		return coords;
	}

	/** [delta_scaled, nextIndex] */
	private static int[] decodeNextDelta(String encoded, int index) {
		int result = 0;
		int shift = 0;
		int b;
		do {
			if (index >= encoded.length()) {
				throw new IllegalArgumentException("잘린 폴리라인 문자열입니다.");
			}
			b = encoded.charAt(index++) - 63;
			result |= (b & 0x1f) << shift;
			shift += 5;
		} while (b >= 0x20);
		int delta = ((result & 1) != 0) ? ~(result >> 1) : (result >> 1);
		return new int[] { delta, index };
	}
}
