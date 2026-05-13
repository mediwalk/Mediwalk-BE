package com.example.mediwalk_be.domain.walk.dto.request;

import com.example.mediwalk_be.domain.walk.entity.enums.ActivityLevel;
import jakarta.validation.constraints.NotNull;

public record RouteFilterRequest(
		@NotNull ActivityLevel activityLevel,
		Boolean includeRestPoints,
		Boolean notifyEcoMart,
		Boolean notifyWalkingProgress
) {
}
