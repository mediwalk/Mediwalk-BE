package com.example.mediwalk_be.domain.walk.dto.response;

/** 폴리라인 경로 따라 찾은 마트·공원 제안 항목. */
public record AlongRoutePoiResponse(
		String poiKey,
		String name,
		AlongRouteSuggestionCategory category,
		double latitude,
		double longitude,
		/** 경로 시작 쪽 꼭짓점까지의 대략 누적 거리(미터) */
		Integer approxAlongRouteMeters,
		/** 폴리라인(선분)까지의 최단 거리(미터) */
		Integer distanceToPolylineMeters
) {
	public enum AlongRouteSuggestionCategory {
		MARKET,
		PARK
	}
}
