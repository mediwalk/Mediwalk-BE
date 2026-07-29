package com.example.mediwalk_be.domain.walk.dto.response;

import com.example.mediwalk_be.domain.walk.entity.Route;
import com.example.mediwalk_be.domain.walk.entity.enums.ActivityLevel;

import java.time.LocalDateTime;
import java.util.List;

public record RouteResponse(
		Long id,
		Long userId,
		Long userDailyMissionId,
		Long destinationId,
		String destinationName,
		Double startLatitude,
		Double startLongitude,
		Integer totalDistanceMeters,
		Integer estimatedWalkTimeMinutes,
		Integer estimatedSteps,
		ActivityLevel activityLevel,
		String routePolyline,
		Boolean hasRestPoints,
		Boolean notifyEcoMart,
		Boolean notifyWalkingProgress,
		List<AlongRoutePoiResponse> martSuggestionsAlongRoute,
		List<AlongRoutePoiResponse> parkSuggestionsAlongRoute,
		List<RouteSegmentResponse> routeSegments,
		LocalDateTime generatedAt,
		LocalDateTime completedAt,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {
	public static RouteResponse from(Route e) {
		return from(e, List.of(), List.of(), List.of());
	}

	public static RouteResponse from(
			Route e,
			List<AlongRoutePoiResponse> martSuggestionsAlongRoute,
			List<AlongRoutePoiResponse> parkSuggestionsAlongRoute) {
		return from(e, martSuggestionsAlongRoute, parkSuggestionsAlongRoute, List.of());
	}

	public static RouteResponse from(
			Route e,
			List<AlongRoutePoiResponse> martSuggestionsAlongRoute,
			List<AlongRoutePoiResponse> parkSuggestionsAlongRoute,
			List<RouteSegmentResponse> routeSegments) {
		return new RouteResponse(
				e.getId(),
				e.getUser().getId(),
				e.getUserDailyMission() != null ? e.getUserDailyMission().getId() : null,
				e.getDestination().getId(),
				e.getDestination().getName(),
				e.getStartLatitude(),
				e.getStartLongitude(),
				e.getTotalDistanceMeters(),
				e.getEstimatedWalkTimeMinutes(),
				e.getEstimatedSteps(),
				e.getActivityLevel(),
				e.getRoutePolyline(),
				e.getHasRestPoints(),
				e.getNotifyEcoMart(),
				e.getNotifyWalkingProgress(),
				martSuggestionsAlongRoute != null ? martSuggestionsAlongRoute : List.of(),
				parkSuggestionsAlongRoute != null ? parkSuggestionsAlongRoute : List.of(),
				routeSegments != null ? routeSegments : List.of(),
				e.getGeneratedAt(),
				e.getCompletedAt(),
				e.getCreatedAt(),
				e.getUpdatedAt()
		);
	}
}
