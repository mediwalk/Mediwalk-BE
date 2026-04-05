package com.example.mediwalk_be.domain.walk.dto.request;

import com.example.mediwalk_be.domain.walk.entity.enums.ActivityLevel;
import com.example.mediwalk_be.domain.walk.entity.enums.SlopeLevel;

public record RouteFilterRequest(
	ActivityLevel activityLevel,
	SlopeLevel slopeLevel,
	Boolean includeRestPoints,
	Boolean natureFriendly,
	Boolean pedestrianOnly
) {
}
