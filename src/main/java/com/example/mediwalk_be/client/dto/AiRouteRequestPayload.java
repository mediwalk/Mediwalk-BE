package com.example.mediwalk_be.client.dto;

import com.example.mediwalk_be.entity.enums.ActivityLevel;
import com.example.mediwalk_be.entity.enums.SlopeLevel;

import java.util.List;

public record AiRouteRequestPayload(
	Double currentLatitude,
	Double currentLongitude,
	List<Long> destinationIds,
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
