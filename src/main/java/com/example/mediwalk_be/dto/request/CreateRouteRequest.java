package com.example.mediwalk_be.dto.request;

import com.example.mediwalk_be.entity.enums.ActivityLevel;
import com.example.mediwalk_be.entity.enums.SlopeLevel;

public record CreateRouteRequest(
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
	Boolean hasRestPoints
) {
}
