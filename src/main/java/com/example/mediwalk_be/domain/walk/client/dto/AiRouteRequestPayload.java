package com.example.mediwalk_be.domain.walk.client.dto;

import com.example.mediwalk_be.domain.walk.entity.enums.ActivityLevel;
import com.example.mediwalk_be.domain.walk.entity.enums.SlopeLevel;

import java.util.List;

public record AiRouteRequestPayload(
	Double currentLatitude,
	Double currentLongitude,
	List<Long> destinationIds,
	Double destinationLatitude,
	Double destinationLongitude,
	FilterPayload filter
) {
	public record FilterPayload(
		ActivityLevel activityLevel,
		SlopeLevel slopeLevel,
		Boolean includeRestPoints,
		Boolean natureFriendly,
		Boolean pedestrianOnly
	) {
	}
}
