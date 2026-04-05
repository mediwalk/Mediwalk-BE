package com.example.mediwalk_be.domain.walk.util;

/**
 * 거리 계산 유틸리티 클래스
 */
public class DistanceUtil {

	private static final double EARTH_RADIUS_METERS = 6371000; // 지구 반지름 (미터)

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
}
