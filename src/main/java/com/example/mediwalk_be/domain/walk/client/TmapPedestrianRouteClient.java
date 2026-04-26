package com.example.mediwalk_be.domain.walk.client;

import com.example.mediwalk_be.domain.walk.client.dto.tmap.TmapPedestrianRouteResult;

public interface TmapPedestrianRouteClient {

	/**
	 * @param startLongitude WGS84 경도 (startX)
	 * @param startLatitude  WGS84 위도 (startY)
	 * @param endLongitude   WGS84 경도 (endX)
	 * @param endLatitude    WGS84 위도 (endY)
	 * @param searchOption   Tmap 문서값: 0 추천, 10 최단, 30 최단+계단제외 등
	 */
	TmapPedestrianRouteResult fetchRoute(
			double startLongitude,
			double startLatitude,
			double endLongitude,
			double endLatitude,
			String startName,
			String endName,
			String searchOption
	);
}
