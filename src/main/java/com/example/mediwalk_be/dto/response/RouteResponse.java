package com.example.mediwalk_be.dto.response;

import com.example.mediwalk_be.entity.Route;
import com.example.mediwalk_be.entity.enums.ActivityLevel;
import com.example.mediwalk_be.entity.enums.SlopeLevel;

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
	SlopeLevel averageSlope,
	ActivityLevel activityLevel,
	String routePolyline,
	Double greenSpaceRatio,
	Integer crosswalkCount,
	Boolean isPedestrianOnly,
	Boolean isNatureFriendly,
	Boolean hasRestPoints,
	List<PointOfInterestResponse> restPoints,
	LocalDateTime generatedAt,
	LocalDateTime completedAt,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static RouteResponse from(Route e) {
		return from(e, null);
	}

	public static RouteResponse from(Route e, List<PointOfInterestResponse> restPoints) {
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
			e.getAverageSlope(),
			e.getActivityLevel(),
			e.getRoutePolyline(),
			e.getGreenSpaceRatio(),
			e.getCrosswalkCount(),
			e.getIsPedestrianOnly(),
			e.getIsNatureFriendly(),
			e.getHasRestPoints(),
			restPoints != null ? restPoints : List.of(),
			e.getGeneratedAt(),
			e.getCompletedAt(),
			e.getCreatedAt(),
			e.getUpdatedAt()
		);
	}
}
