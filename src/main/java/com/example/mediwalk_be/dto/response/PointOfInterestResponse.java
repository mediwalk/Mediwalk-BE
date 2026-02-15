package com.example.mediwalk_be.dto.response;

import com.example.mediwalk_be.entity.PointOfInterest;
import com.example.mediwalk_be.entity.enums.PointOfInterestType;

import java.time.LocalDateTime;

public record PointOfInterestResponse(
	Long id,
	Long routeId,
	String name,
	PointOfInterestType type,
	Double latitude,
	Double longitude,
	Integer order,
	Integer distanceFromPrevious,
	String instruction,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static PointOfInterestResponse from(PointOfInterest e) {
		return new PointOfInterestResponse(
			e.getId(),
			e.getRoute().getId(),
			e.getName(),
			e.getType(),
			e.getLatitude(),
			e.getLongitude(),
			e.getOrder(),
			e.getDistanceFromPrevious(),
			e.getInstruction(),
			e.getCreatedAt(),
			e.getUpdatedAt()
		);
	}
}
