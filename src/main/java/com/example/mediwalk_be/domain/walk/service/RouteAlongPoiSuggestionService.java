package com.example.mediwalk_be.domain.walk.service;

import com.example.mediwalk_be.domain.walk.client.TmapAroundPoiClient;
import com.example.mediwalk_be.domain.walk.client.dto.tmap.TmapPoiBrief;
import com.example.mediwalk_be.domain.walk.dto.response.AlongRoutePoiResponse;
import com.example.mediwalk_be.domain.walk.util.DistanceUtil;
import com.example.mediwalk_be.domain.walk.util.GeoPathSampling;
import com.example.mediwalk_be.domain.walk.util.PolylineDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
public class RouteAlongPoiSuggestionService {

	private static final String[] CONVENIENCE_HINTS =
			new String[] { "편의점", "씨유", "gs25", "gs 25", "세븐일레븐", "7 eleven", "7-eleven", "이마트24", "emart24",
					"미니스톱", "바이더웨이" };

	private final TmapAroundPoiClient tmapAroundPoiClient;
	private final double sampleIntervalMeters;
	private final int sampleMaxPoints;
	private final String searchRadiusKm;
	private final String martCategories;
	private final String parkCategories;
	private final int poiCountPerCall;
	private final int maxSuggestionsPerCategory;

	public RouteAlongPoiSuggestionService(
			TmapAroundPoiClient tmapAroundPoiClient,
			@Value("${app.tmap.poi-sample-interval-meters:260}") double sampleIntervalMeters,
			@Value("${app.tmap.poi-sample-max-points:14}") int sampleMaxPoints,
			@Value("${app.tmap.poi-search-radius-km:1}") String searchRadiusKm,
			@Value("${app.tmap.poi-mart-categories:대형마트;마트}") String martCategories,
			@Value("${app.tmap.poi-park-categories:공원}") String parkCategories,
			@Value("${app.tmap.poi-search-count-per-call:18}") int poiCountPerCall,
			@Value("${app.tmap.poi-max-suggestions-per-category:1}") int maxSuggestionsPerCategory) {
		this.tmapAroundPoiClient = tmapAroundPoiClient;
		this.sampleIntervalMeters = sampleIntervalMeters;
		this.sampleMaxPoints = sampleMaxPoints;
		this.searchRadiusKm = searchRadiusKm;
		this.martCategories = martCategories;
		this.parkCategories = parkCategories;
		this.poiCountPerCall = poiCountPerCall;
		this.maxSuggestionsPerCategory = Math.max(1, maxSuggestionsPerCategory);
	}

	public List<AlongRoutePoiResponse> collectMartSuggestions(String encodedPolyline) {
		return collect(encodedPolyline, AlongRoutePoiResponse.AlongRouteSuggestionCategory.MARKET);
	}

	public List<AlongRoutePoiResponse> collectParkSuggestions(String encodedPolyline) {
		return collect(encodedPolyline, AlongRoutePoiResponse.AlongRouteSuggestionCategory.PARK);
	}

	private List<AlongRoutePoiResponse> collect(String encodedPolyline, AlongRoutePoiResponse.AlongRouteSuggestionCategory category) {
		if (encodedPolyline == null || encodedPolyline.isBlank()) {
			return List.of();
		}

		List<double[]> path;
		try {
			path = PolylineDecoder.decodeToLatLonPairs(encodedPolyline);
		} catch (Exception e) {
			log.warn("폴리라인 디코딩 실패: {}", e.getMessage());
			return List.of();
		}
		if (path.size() < 2) {
			return List.of();
		}

		List<double[]> samples =
				GeoPathSampling.sampleAlongPath(path, sampleIntervalMeters, sampleMaxPoints);
		String categories =
				category == AlongRoutePoiResponse.AlongRouteSuggestionCategory.MARKET ? martCategories : parkCategories;

		Map<String, TmapPoiBrief> uniq = new LinkedHashMap<>();

		for (double[] xy : samples) {
			List<TmapPoiBrief> hits = tmapAroundPoiClient.searchAround(
					xy[0],
					xy[1],
					searchRadiusKm,
					categories,
					poiCountPerCall);

			for (TmapPoiBrief p : hits) {
				if (category == AlongRoutePoiResponse.AlongRouteSuggestionCategory.MARKET && isLikelyConvenienceStore(p.name())) {
					continue;
				}
				/* 공원은 이미 T map categories(기본 "공원")로 한정됨 — 이름 휴리스틱 재필터링 시 결과가 전부 비는 경우가 있음 */
				String key = !p.pkey().isBlank() ? p.pkey() : (!p.id().isBlank() ? p.id() : p.name());
				uniq.merge(key, p,
						(a, b) -> DistanceUtil.minDistanceMetersToPolyline(path, a.latitude(), a.longitude())
								<= DistanceUtil.minDistanceMetersToPolyline(path, b.latitude(), b.longitude()) ? a : b);
			}
		}

		if (uniq.isEmpty()) {
			log.warn(
					"경로 POI 후보 병합 결과 0건: category={}, pathVertices={}, samplePoints={}, 첫샘플=({}, {}), categories={}, radiusKm={} — Tmap POI WARN 로그·app.tmap.api-key·폴리라인 확인",
					category,
					path.size(),
					samples.size(),
					samples.isEmpty() ? Double.NaN : samples.get(0)[0],
					samples.isEmpty() ? Double.NaN : samples.get(0)[1],
					categories,
					searchRadiusKm);
		}

		List<AlongRoutePoiResponse> out = new ArrayList<>();
		for (TmapPoiBrief p : uniq.values()) {
			int along = approximateAlongRouteMeters(path, p.latitude(), p.longitude());
			double dPath = DistanceUtil.minDistanceMetersToPolyline(path, p.latitude(), p.longitude());
			int distPoly = (int) Math.round(Math.min(dPath, Integer.MAX_VALUE));
			String key = !p.pkey().isBlank() ? p.pkey() : (!p.id().isBlank() ? p.id() : p.name());
			out.add(new AlongRoutePoiResponse(key, p.name(), category, p.latitude(), p.longitude(), along, distPoly));
		}

		out.sort(Comparator.comparingInt(AlongRoutePoiResponse::distanceToPolylineMeters));
		if (out.size() > maxSuggestionsPerCategory) {
			return List.copyOf(out.subList(0, maxSuggestionsPerCategory));
		}
		return List.copyOf(out);
	}

	private static boolean isLikelyConvenienceStore(String name) {
		String n = name == null ? "" : name.toLowerCase(Locale.ROOT);
		for (String h : CONVENIENCE_HINTS) {
			if (n.contains(h)) {
				return true;
			}
		}
		return false;
	}

	static int approximateAlongRouteMeters(List<double[]> pathLatLon, double lat, double lon) {
		double bestDist = Double.MAX_VALUE;
		int closestIdx = 0;
		for (int i = 0; i < pathLatLon.size(); i++) {
			double[] v = pathLatLon.get(i);
			double d = DistanceUtil.calculateDistanceMeters(lat, lon, v[0], v[1]);
			if (d < bestDist) {
				bestDist = d;
				closestIdx = i;
			}
		}
		double cum = 0;
		for (int j = 0; j < closestIdx; j++) {
			double[] a = pathLatLon.get(j);
			double[] b = pathLatLon.get(j + 1);
			cum += DistanceUtil.calculateDistanceMeters(a[0], a[1], b[0], b[1]);
		}
		return Math.max(0, (int) Math.round(cum));
	}

}
