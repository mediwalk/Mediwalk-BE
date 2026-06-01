package com.example.mediwalk_be.domain.walk.util;

import java.util.List;

/**
 * 거리 계산 유틸리티 클래스
 */
public class DistanceUtil {

	private static final double EARTH_RADIUS_METERS = 6371000; // 지구 반지름 (미터)
	private static final double METERS_PER_DEG_LAT = 111_320.0;

	/**
	 * 두 좌표 간의 거리를 미터 단위로 계산 (Haversine 공식)
	 *
	 * @param lat1 첫 번째 지점의 위도
	 * @param lon1 첫 번째 지점의 경도
	 * @param lat2 두 번째 지점의 위도
	 * @param lon2 두 번째 지점의 경도
	 * @return 두 지점 간의 거리 (미터)
	 */
	public static double calculateDistanceMeters(double lat1, double lon1, double lat2, double lon2) {
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
				* Math.sin(dLon / 2) * Math.sin(dLon / 2);

		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		return EARTH_RADIUS_METERS * c;
	}

	/**
	 * 두 좌표 간의 거리가 지정된 반경 이내인지 확인
	 *
	 * @param lat1 첫 번째 지점의 위도
	 * @param lon1 첫 번째 지점의 경도
	 * @param lat2 두 번째 지점의 위도
	 * @param lon2 두 번째 지점의 경도
	 * @param radiusMeters 반경 (미터)
	 * @return 반경 이내이면 true, 아니면 false
	 */
	public static boolean isWithinRadius(double lat1, double lon1, double lat2, double lon2, double radiusMeters) {
		double distance = calculateDistanceMeters(lat1, lon1, lat2, lon2);
		return distance <= radiusMeters;
	}

	/**
	 * 점에서 위경도 꺾은선(연속 선분)까지의 최단 거리(미터).
	 * 도보 구간처럼 짧은 링크에서는 경도 스케일에 cos(위도)를 적용한 국소 평면 근사를 사용합니다.
	 */
	public static double minDistanceMetersToPolyline(List<double[]> pathLatLon, double pLat, double pLon) {
		if (pathLatLon == null || pathLatLon.size() < 2) {
			return Double.POSITIVE_INFINITY;
		}
		double min = Double.POSITIVE_INFINITY;
		for (int i = 0; i < pathLatLon.size() - 1; i++) {
			double[] a = pathLatLon.get(i);
			double[] b = pathLatLon.get(i + 1);
			double d = pointToSegmentDistanceMeters(a[0], a[1], b[0], b[1], pLat, pLon);
			if (d < min) {
				min = d;
			}
		}
		return min;
	}

	private static double pointToSegmentDistanceMeters(
			double lat1, double lon1, double lat2, double lon2,
			double pLat, double pLon) {
		double midLatRad = Math.toRadians((lat1 + lat2 + pLat) / 3.0);
		double mPerDegLon = METERS_PER_DEG_LAT * Math.cos(midLatRad);

		double ax = 0.0;
		double ay = 0.0;
		double bx = (lon2 - lon1) * mPerDegLon;
		double by = (lat2 - lat1) * METERS_PER_DEG_LAT;
		double px = (pLon - lon1) * mPerDegLon;
		double py = (pLat - lat1) * METERS_PER_DEG_LAT;

		double dx = bx - ax;
		double dy = by - ay;
		double len2 = dx * dx + dy * dy;
		if (len2 < 1e-12) {
			return Math.hypot(px - ax, py - ay);
		}
		double t = (px * dx + py * dy) / len2;
		t = Math.max(0.0, Math.min(1.0, t));
		double qx = ax + t * dx;
		double qy = ay + t * dy;
		return Math.hypot(px - qx, py - qy);
	}
}
