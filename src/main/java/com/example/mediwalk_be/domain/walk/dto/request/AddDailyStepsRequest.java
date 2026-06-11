package com.example.mediwalk_be.domain.walk.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;

public record AddDailyStepsRequest(
	@Positive @Max(20_000) int count
) {
}
