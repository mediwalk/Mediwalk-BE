package com.example.mediwalk_be.dto.request;

import com.example.mediwalk_be.entity.enums.ActivityLevel;
import com.example.mediwalk_be.entity.enums.SlopeLevel;

public record RouteFilterRequest(
	ActivityLevel activityLevel,
	SlopeLevel slopeLevel,
	Boolean includeRestPoints,
	Boolean natureFriendly,
	Boolean pedestrianOnly
) {
}
