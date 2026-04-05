package com.example.mediwalk_be.domain.walk.dto.response;

import com.example.mediwalk_be.domain.walk.entity.enums.ActivityLevel;
import com.example.mediwalk_be.domain.walk.entity.enums.SlopeLevel;

public record AiRouteGenerationResponse(
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
