package com.example.mediwalk_be.domain.walk.dto.response;

import com.example.mediwalk_be.domain.walk.entity.CollectionLocation;
import com.example.mediwalk_be.domain.walk.util.DistanceUtil;

public record DestinationProximityResponse(
		Long destinationId,
		String destinationName,
		Integer distanceMeters,
		Integer activationRadiusMeters,
		Boolean withinActivationRadius
) {
	public static DestinationProximityResponse from(
			CollectionLocation destination,
			double currentLatitude,
			double currentLongitude) {
		int activationRadius = destination.getActivationRadius() != null
				? destination.getActivationRadius()
				: 20;
		double distance = DistanceUtil.calculateDistanceMeters(
				currentLatitude,
				currentLongitude,
				destination.getLatitude(),
				destination.getLongitude()
		);
		return new DestinationProximityResponse(
				destination.getId(),
				destination.getName(),
				(int) Math.round(distance),
				activationRadius,
				distance <= activationRadius
		);
	}
}
