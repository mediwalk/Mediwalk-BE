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
		List<PointOfInterestResponse> restPoints,
		List<AlongRoutePoiResponse> martSuggestionsAlongRoute,
		List<AlongRoutePoiResponse> parkSuggestionsAlongRoute,
		LocalDateTime generatedAt,
		LocalDateTime completedAt,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {
	public static RouteResponse from(Route e) {
		return from(e, null, List.of(), List.of());
	}

	public static RouteResponse from(Route e, List<PointOfInterestResponse> restPoints) {
		return from(e, restPoints, List.of(), List.of());
	}

	public static RouteResponse from(
			Route e,
			List<PointOfInterestResponse> restPoints,
			List<AlongRoutePoiResponse> martSuggestionsAlongRoute,
			List<AlongRoutePoiResponse> parkSuggestionsAlongRoute) {
		return new RouteResponse(
				e.getId(),
				e.getUser().getId(),
				e.getUserDailyMission() != null ? e.getUserDailyMission().getId() : null,
				e.getDestination().getId(),
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
				restPoints != null ? restPoints : List.of(),
				martSuggestionsAlongRoute != null ? martSuggestionsAlongRoute : List.of(),
				parkSuggestionsAlongRoute != null ? parkSuggestionsAlongRoute : List.of(),
				e.getGeneratedAt(),
				e.getCompletedAt(),
				e.getCreatedAt(),
				e.getUpdatedAt()
		);
	}
}
