package com.example.mediwalk_be.domain.walk.service;

import com.example.mediwalk_be.domain.walk.client.TmapPedestrianRouteClient;
import com.example.mediwalk_be.domain.walk.client.dto.tmap.TmapPedestrianRouteResult;
import com.example.mediwalk_be.domain.walk.dto.response.TmapRoutePreviewResponse;
import com.example.mediwalk_be.domain.walk.util.PolylineEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TmapPedestrianRouteService {

	private static final double METERS_PER_STEP = 0.75;

	private final TmapPedestrianRouteClient tmapPedestrianRouteClient;

	public TmapRoutePreviewResponse preview(
			double startLatitude,
			double startLongitude,
			double endLatitude,
			double endLongitude,
			String startName,
			String endName,
			String searchOption) {
		TmapPedestrianRouteResult route = tmapPedestrianRouteClient.fetchRoute(
				startLongitude,
				startLatitude,
				endLongitude,
				endLatitude,
				startName,
				endName,
				searchOption);

		List<double[]> latLng = route.pathLatLng();
		String encoded = PolylineEncoder.encodeLatLngPairs(latLng);

		int distance = route.totalDistanceMeters();
		int timeSec = route.totalTimeSeconds();
		int minutes = (timeSec + 59) / 60;
		int steps = distance > 0 ? (int) Math.round(distance / METERS_PER_STEP) : 0;

		return new TmapRoutePreviewResponse(
				distance,
				timeSec,
				minutes,
				steps,
				encoded,
				latLng.size(),
				route.guideSteps() != null ? route.guideSteps() : List.of());
	}
}
