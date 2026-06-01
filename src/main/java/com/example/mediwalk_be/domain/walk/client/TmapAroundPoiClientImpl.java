package com.example.mediwalk_be.domain.walk.client;

import com.example.mediwalk_be.domain.walk.client.dto.tmap.TmapPoiBrief;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TmapAroundPoiClientImpl implements TmapAroundPoiClient {

	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;
	private final String baseUrl;
	private final String apiKey;

	public TmapAroundPoiClientImpl(
			RestTemplate restTemplate,
			ObjectMapper objectMapper,
			@Value("${app.tmap.poi-around-search-url:https://apis.openapi.sk.com/tmap/pois/search/around}") String baseUrl,
			@Value("${app.tmap.api-key:}") String apiKey) {
		this.restTemplate = restTemplate;
		this.objectMapper = objectMapper;
		this.baseUrl = baseUrl == null ? "" : baseUrl.trim();
		this.apiKey = normalizeApiKey(apiKey);
		if (this.apiKey.isEmpty()) {
			log.warn("Tmap 주변 POI: app.tmap.api-key 비어 있음 → POI 후보는 항상 빈 배열 (application-local.yaml 또는 TMAP_APP_KEY 확인)");
		} else {
			log.info("Tmap 주변 POI: appKey 로드됨(키 미표시, 길이만 {})", this.apiKey.length());
		}
	}

	@Override
	public List<TmapPoiBrief> searchAround(
			double centerLatitude,
			double centerLongitude,
			String radiusKm,
			String categories,
			int count) {
		if (apiKey.isEmpty() || baseUrl.isEmpty()) {
			log.warn("Tmap POI 검색 건너뜀: API 키 또는 URL 미설정");
			return List.of();
		}
		int cnt = Math.max(1, Math.min(count, 50));

		if (categories == null || categories.isBlank()) {
			return List.of();
		}

		String radiusParam = normalizeRadiusKmForSkAroundSearch(radiusKm);

		// ';'(대형마트;마트)·한글: UriComponents 기본 encode는 ';'를 남길 수 있어 쿼리 값만 RFC에 맞게 인코딩
		String encodedCategories = UriUtils.encodeQueryParam(categories, StandardCharsets.UTF_8);
		URI uri = UriComponentsBuilder.fromUriString(baseUrl)
				.queryParam("version", "1")
				.queryParam("page", "1")
				.queryParam("count", Integer.toString(cnt))
				.queryParam("centerLat", Double.toString(centerLatitude))
				.queryParam("centerLon", Double.toString(centerLongitude))
				.queryParam("radius", radiusParam)
				.queryParam("reqCoordType", "WGS84GEO")
				.queryParam("resCoordType", "WGS84GEO")
				.queryParam("multiPoint", "N")
				.queryParam("categories", encodedCategories)
				.build(true)
				.toUri();

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));
		headers.set("appKey", apiKey);
		HttpEntity<Void> entity = new HttpEntity<>(headers);

		try {
			ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
			if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
				return List.of();
			}
			return parsePois(response.getBody());
		} catch (RestClientResponseException e) {
			String snippet = describeErrorBody(e.getResponseBodyAsByteArray(), e.getResponseBodyAsString());
			log.warn("Tmap POI search HTTP {} {}: {}", e.getStatusCode(), uri.getPath(), snippet);
			return List.of();
		} catch (Exception e) {
			log.warn("Tmap POI 검색 파싱/호출 오류", e);
			return List.of();
		}
	}

	private List<TmapPoiBrief> parsePois(String body) throws Exception {
		JsonNode root = objectMapper.readTree(body);
		if (root.has("errorMessage")) {
			log.warn("Tmap POI errorMessage={}", root.get("errorMessage").asText(""));
			return List.of();
		}
		JsonNode searchInfo = root.get("searchPoiInfo");
		if (searchInfo == null) {
			log.warn("Tmap POI 응답에 searchPoiInfo 없음 (JSON 모양 확인)");
			return List.of();
		}
		JsonNode pois = searchInfo.get("pois");
		if (pois == null) {
			return List.of();
		}
		JsonNode poiArr = pois.get("poi");
		List<JsonNode> nodes = flattenPoiArray(poiArr);
		List<TmapPoiBrief> out = new ArrayList<>(nodes.size());
		for (JsonNode n : nodes) {
			String id = text(n.get("id"));
			String pkey = text(n.get("pkey"));
			String name = text(n.get("name"));
			String bizName = text(n.get("bizName"));
			JsonNode plat = n.get("frontLat");
			JsonNode plon = n.get("frontLon");
			if (plat == null || plat.isNull()) {
				plat = n.get("noorLat");
			}
			if (plon == null || plon.isNull()) {
				plon = n.get("noorLon");
			}
			if (plat == null || plat.isNull() || plon == null || plon.isNull()) {
				continue;
			}
			double lat = parseDoubleFlexible(plat);
			double lon = parseDoubleFlexible(plon);
			if (Double.isNaN(lat) || Double.isNaN(lon) || name.isBlank()) {
				continue;
			}
			out.add(new TmapPoiBrief(id, pkey.isBlank() ? id : pkey, name, bizName, lat, lon));
		}
		return out;
	}

	private static List<JsonNode> flattenPoiArray(JsonNode poiNode) {
		if (poiNode == null || poiNode.isNull()) {
			return List.of();
		}
		if (poiNode.isArray()) {
			List<JsonNode> r = new ArrayList<>();
			for (JsonNode x : poiNode) {
				r.add(x);
			}
			return r;
		}
		return List.of(poiNode);
	}

	private static String text(JsonNode n) {
		return n == null || n.isNull() ? "" : n.asText("").trim();
	}

	private static double parseDoubleFlexible(JsonNode n) {
		if (n == null || n.isNull()) {
			return Double.NaN;
		}
		if (n.isNumber()) {
			return n.doubleValue();
		}
		try {
			return Double.parseDouble(n.asText().trim());
		} catch (Exception e) {
			return Double.NaN;
		}
	}

	/**
	 * SK 오픈API「주변 카테고리 검색」: radius는 정수 km만 허용 (1~33), 전국은 0.
	 * 소수(예: 0.4)를 보내면 HTTP 400이 나는 경우가 있음.
	 */
	private static String normalizeRadiusKmForSkAroundSearch(String radiusKm) {
		if (radiusKm == null || radiusKm.isBlank()) {
			return "1";
		}
		try {
			double v = Double.parseDouble(radiusKm.trim());
			if (v <= 0) {
				return "0";
			}
			int km = (int) Math.ceil(v);
			if (km < 1) {
				km = 1;
			}
			if (km > 33) {
				km = 33;
			}
			return Integer.toString(km);
		} catch (NumberFormatException e) {
			return "1";
		}
	}

	private static String describeErrorBody(byte[] rawBytes, String asString) {
		if (rawBytes != null && rawBytes.length >= 2 && (rawBytes[0] & 0xFF) == 0x1F && (rawBytes[1] & 0xFF) == 0x8B) {
			return "(응답 본문 gzip — 오류 상세는 decompress 필요. 요청 파라미터 예: radius는 정수 km 1~33 또는 0)";
		}
		String snippet = asString != null ? asString : "";
		if (snippet.length() > 400) {
			snippet = snippet.substring(0, 400) + "...";
		}
		return snippet.isBlank() ? "(본문 없음)" : snippet;
	}

	private static String normalizeApiKey(String raw) {
		if (raw == null) {
			return "";
		}
		String s = raw.trim();
		if (s.startsWith("\uFEFF")) {
			s = s.substring(1).trim();
		}
		if (s.length() >= 2
				&& ((s.startsWith("'") && s.endsWith("'")) || (s.startsWith("\"") && s.endsWith("\"")))) {
			s = s.substring(1, s.length() - 1).trim();
		}
		return s;
	}
}
