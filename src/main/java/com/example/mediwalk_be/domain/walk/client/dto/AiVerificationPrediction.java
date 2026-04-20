package com.example.mediwalk_be.domain.walk.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiVerificationPrediction(
		List<Double> confidences,
		List<List<Double>> bboxes,
		List<String> ids,
		List<String> displayNames
) {}
