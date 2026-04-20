package com.example.mediwalk_be.domain.walk.dto.request;

import com.example.mediwalk_be.domain.walk.entity.enums.ActivityLevel;
import com.example.mediwalk_be.domain.walk.entity.enums.SlopeLevel;
import jakarta.validation.constraints.NotNull;

public record RouteFilterRequest(
	@NotNull ActivityLevel activityLevel,
	@NotNull SlopeLevel slopeLevel,
	Boolean includeRestPoints,
	Boolean natureFriendly,
	Boolean pedestrianOnly
) {
}
