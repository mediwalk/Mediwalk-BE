package com.example.mediwalk_be.dto.response;

import com.example.mediwalk_be.entity.RouteFilter;
import com.example.mediwalk_be.entity.enums.ActivityLevel;
import com.example.mediwalk_be.entity.enums.SlopeLevel;

import java.time.LocalDateTime;

public record RouteFilterResponse(
	Long id,
	Long routeId,
	ActivityLevel activityLevel,
	SlopeLevel slopeLevel,
	Boolean includeRestPoints,
	Boolean natureFriendly,
	Boolean pedestrianOnly,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static RouteFilterResponse from(RouteFilter e) {
		return new RouteFilterResponse(
			e.getId(),
			e.getRoute().getId(),
			e.getActivityLevel(),
			e.getSlopeLevel(),
			e.getIncludeRestPoints(),
			e.getNatureFriendly(),
			e.getPedestrianOnly(),
			e.getCreatedAt(),
			e.getUpdatedAt()
		);
	}
}
