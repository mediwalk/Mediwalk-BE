package com.example.mediwalk_be.domain.walk.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiVerificationResponse(
		List<AiVerificationPrediction> predictions
) {}
