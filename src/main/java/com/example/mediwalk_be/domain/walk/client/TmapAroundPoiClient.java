package com.example.mediwalk_be.domain.walk.client;

import com.example.mediwalk_be.domain.walk.client.dto.tmap.TmapPoiBrief;

import java.util.List;

/** Tmap 명칭(POI) 주변 카테고리 검색. */
public interface TmapAroundPoiClient {

	List<TmapPoiBrief> searchAround(
			double centerLatitude,
			double centerLongitude,
			String radiusKm,
			String categories,
			int count
	);
}
