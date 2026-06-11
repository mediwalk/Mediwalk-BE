package com.example.mediwalk_be.domain.walk.dto.response;

import com.example.mediwalk_be.domain.walk.client.dto.tmap.TmapRouteGuideStep;
import com.example.mediwalk_be.domain.walk.entity.Route;

import java.util.List;

public record GeneratedRouteResponse(
		Route route,
		List<TmapRouteGuideStep> guideSteps
) {
}
