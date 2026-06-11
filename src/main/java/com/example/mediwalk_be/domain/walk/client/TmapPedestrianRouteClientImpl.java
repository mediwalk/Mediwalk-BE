package com.example.mediwalk_be.domain.walk.client;

import com.example.mediwalk_be.domain.walk.client.dto.tmap.TmapPedestrianRouteResult;
import com.example.mediwalk_be.domain.walk.client.dto.tmap.TmapRouteGuideStep;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class TmapPedestrianRouteClientImpl implements TmapPedestrianRouteClient {

	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;
	private final String pedestrianRouteBaseUrl;
	private final String apiKey;

	public TmapPedestrianRouteClientImpl(
			RestTemplate restTemplate,
			ObjectMapper objectMapper,
			@Value("${app.tmap.pedestrian-route-url:https://apis.openapi.sk.com/tmap/routes/pedestrian}") String pedestrianRouteBaseUrl,
			@Value("${app.tmap.api-key:}") String apiKey) {
		this.restTemplate = restTemplate;
		this.objectMapper = objectMapper;
		this.pedestrianRouteBaseUrl = pedestrianRouteBaseUrl == null ? "" : pedestrianRouteBaseUrl.trim();
		this.apiKey = normalizeApiKey(apiKey);
	}

	@Override
	public TmapPedestrianRouteResult fetchRoute(
			double startLongitude,
			double startLatitude,
			double endLongitude,
			double endLatitude,
			String startName,
			String endName,
			String searchOption) {
		if (apiKey.isEmpty()) {
			throw new IllegalStateException("Tmap API 키가 설정되지 않았습니다. app.tmap.api-key 또는 TMAP_APP_KEY 를 설정하세요.");
		}
		if (pedestrianRouteBaseUrl.isEmpty()) {
			throw new IllegalStateException("Tmap 보행자 경로 URL이 비어 있습니다.");
		}

		// SK 오픈API 샘플: Accept/Content-Type application/json, 바디 JSON (form 아님)
		// SK 샘플(curl)과 동일: version=1 & callback=function
		URI uri = UriComponentsBuilder.fromUriString(pedestrianRouteBaseUrl)
				.queryParam("version", "1")
				.queryParam("callback", "function")
				.build()
				.toUri();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));
		headers.set("appKey", apiKey);

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("startX", startLongitude);
		body.put("startY", startLatitude);
		body.put("endX", endLongitude);
		body.put("endY", endLatitude);
		body.put("startName", startName != null && !startName.isBlank() ? startName : "출발");
		body.put("endName", endName != null && !endName.isBlank() ? endName : "도착");
		body.put("reqCoordType", "WGS84GEO");
		body.put("resCoordType", "WGS84GEO");
		body.put("searchOption", searchOption != null && !searchOption.isBlank() ? searchOption : "0");
		body.put("sort", "index");
		body.put("angle", 20);
		body.put("speed", 30);

		String jsonBody;
		try {
			jsonBody = objectMapper.writeValueAsString(body);
		} catch (Exception e) {
			throw new IllegalStateException("Tmap 요청 JSON 직렬화 실패", e);
		}

		HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

		try {
			ResponseEntity<String> response = restTemplate.postForEntity(uri, entity, String.class);
			if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
				throw new IllegalStateException("Tmap 응답 오류: " + response.getStatusCode());
			}
			return parseResponse(response.getBody());
		} catch (RestClientResponseException e) {
			String snippet = e.getResponseBodyAsString();
			if (snippet != null && snippet.length() > 500) {
				snippet = snippet.substring(0, 500) + "...";
			}
			throw new IllegalStateException(
					"Tmap 호출 실패 HTTP " + e.getStatusCode().value() + ": " + snippet, e);
		}
	}

	private TmapPedestrianRouteResult parseResponse(String json) {
		try {
			JsonNode root = objectMapper.readTree(json);
			if (root.has("error") || root.has("errorMessage")) {
				String msg = root.has("errorMessage") ? root.get("errorMessage").asText() : root.get("error").toString();
				throw new IllegalStateException("Tmap 오류 응답: " + msg);
			}
			JsonNode features = root.get("features");
			if (features == null || !features.isArray()) {
				String head = json.length() > 400 ? json.substring(0, 400) + "..." : json;
				throw new IllegalStateException("Tmap 응답에 features 배열이 없습니다. 응답 앞부분: " + head);
			}

			int totalDistance = 0;
			int totalTime = 0;
			List<double[]> path = new ArrayList<>();
			List<TmapRouteGuideStep> guideSteps = new ArrayList<>();
			int cumulativeMeters = 0;

			for (JsonNode feature : features) {
				JsonNode geometry = feature.get("geometry");
				JsonNode props = feature.get("properties");
				if (geometry == null || !geometry.has("type")) {
					continue;
				}
				String geomType = geometry.get("type").asText();

				if ("LineString".equals(geomType)) {
					JsonNode coordinates = geometry.get("coordinates");
					if (coordinates != null && coordinates.isArray()) {
						for (JsonNode pt : coordinates) {
							if (pt.isArray() && pt.size() >= 2) {
								double lng = pt.get(0).asDouble();
								double lat = pt.get(1).asDouble();
								path.add(new double[] { lat, lng });
							}
						}
					}
					if (props != null) {
						int segDist = props.has("distance")
								? (int) Math.round(props.get("distance").asDouble())
								: 0;
						String instruction = buildLineInstruction(props);
						if (instruction != null) {
							int along = cumulativeMeters + Math.max(segDist / 2, 0);
							guideSteps.add(new TmapRouteGuideStep(along, instruction));
						}
						cumulativeMeters += Math.max(segDist, 0);
					}
				} else if ("Point".equals(geomType) && props != null) {
					if (props.has("totalDistance")) {
						totalDistance = (int) Math.round(props.get("totalDistance").asDouble());
					}
					if (props.has("totalTime")) {
						totalTime = (int) Math.round(props.get("totalTime").asDouble());
					}
					String pointType = textOrEmpty(props.get("pointType"));
					if (!isDestinationPoint(pointType)) {
						String instruction = buildPointInstruction(props);
						if (instruction != null) {
							guideSteps.add(new TmapRouteGuideStep(cumulativeMeters, instruction));
						}
					}
				}
			}

			if (path.isEmpty()) {
				throw new IllegalStateException("Tmap 응답에 LineString 좌표가 없습니다.");
			}
			if (totalDistance <= 0) {
				totalDistance = cumulativeMeters;
			}

			return new TmapPedestrianRouteResult(totalDistance, totalTime, path, List.copyOf(guideSteps));
		} catch (IllegalStateException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException("Tmap 응답 파싱 실패: " + e.getMessage(), e);
		}
	}

	private static String buildLineInstruction(JsonNode props) {
		String description = cleanInstruction(textOrEmpty(props.get("description")));
		String roadName = textOrEmpty(props.get("name"));
		if (!description.isBlank() && isUsefulInstruction(description)) {
			return description;
		}
		if (!roadName.isBlank()) {
			int dist = props.has("distance") ? (int) Math.round(props.get("distance").asDouble()) : 0;
			if (dist > 0) {
				return roadName + "를 따라 " + dist + "m 이동";
			}
			return roadName + "를 따라 이동";
		}
		return null;
	}

	private static String buildPointInstruction(JsonNode props) {
		String description = cleanInstruction(textOrEmpty(props.get("description")));
		if (!description.isBlank() && isUsefulInstruction(description)) {
			return description;
		}
		String nextRoad = textOrEmpty(props.get("nextRoadName"));
		if (!nextRoad.isBlank()) {
			return nextRoad + " 방향으로 이동";
		}
		return null;
	}

	private static boolean isDestinationPoint(String pointType) {
		return "E".equals(pointType) || "EP".equals(pointType);
	}

	private static String cleanInstruction(String raw) {
		if (raw == null || raw.isBlank()) {
			return "";
		}
		String s = raw.trim();
		while (s.startsWith(",")) {
			s = s.substring(1).trim();
		}
		return s;
	}

	private static boolean isUsefulInstruction(String instruction) {
		if (instruction.isBlank() || instruction.length() < 2) {
			return false;
		}
		String compact = instruction.replaceAll("\\s+", "");
		return !compact.matches("^[0-9.,mM]+$");
	}

	private static String textOrEmpty(JsonNode node) {
		return node == null || node.isNull() ? "" : node.asText("").trim();
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
