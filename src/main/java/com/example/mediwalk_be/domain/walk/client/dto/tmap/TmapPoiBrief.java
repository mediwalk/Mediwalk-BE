package com.example.mediwalk_be.domain.walk.client.dto.tmap;

/**
 * 주변 카테고리 POI 검색 한 건 요약 (Tmap /search/around 등).
 */
public record TmapPoiBrief(String id,
		String pkey,
		String name,
		String bizName,
		double latitude,
		double longitude
) {
}
