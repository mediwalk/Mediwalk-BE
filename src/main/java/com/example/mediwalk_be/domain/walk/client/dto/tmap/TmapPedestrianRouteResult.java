package com.example.mediwalk_be.domain.walk.client.dto.tmap;

import java.util.List;

public record TmapPedestrianRouteResult(
		int totalDistanceMeters,
		int totalTimeSeconds,
		List<double[]> pathLatLng, // [lat, lng]
		List<TmapRouteGuideStep> guideSteps
) {
}
