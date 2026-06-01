package com.example.mediwalk_be.domain.walk.dto.request;

import com.example.mediwalk_be.domain.walk.entity.enums.ActivityLevel;

public record CreateRouteRequest(
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
		Boolean notifyWalkingProgress
) {
}
