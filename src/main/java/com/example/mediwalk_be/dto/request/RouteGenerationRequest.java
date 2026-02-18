package com.example.mediwalk_be.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RouteGenerationRequest(
	@NotNull Long userId,
	@NotNull Double currentLatitude,
	@NotNull Double currentLongitude,
	@NotNull List<Long> destinationIds,
	@Valid RouteFilterRequest filter
) {
}
