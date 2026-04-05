package com.example.mediwalk_be.domain.walk.client;

import com.example.mediwalk_be.domain.walk.client.dto.AiRouteRequestPayload;
import com.example.mediwalk_be.domain.walk.dto.request.RouteFilterRequest;
import com.example.mediwalk_be.domain.walk.dto.request.RouteGenerationRequest;
import com.example.mediwalk_be.domain.walk.dto.response.AiRouteGenerationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AiRouteClientImpl implements AiRouteClient {

	private final RestTemplate restTemplate;
	private final String baseUrl;

	public AiRouteClientImpl(
			RestTemplate restTemplate,
			@Value("${app.ai.route-generation-url:}") String baseUrl) {
		this.restTemplate = restTemplate;
		this.baseUrl = baseUrl == null ? "" : baseUrl.trim();
	}

	@Override
	public AiRouteGenerationResponse generate(RouteGenerationRequest request) {
		if (baseUrl.isEmpty()) {
			throw new IllegalArgumentException(
					"AI 경로 생성 서비스 URL이 설정되지 않았습니다. app.ai.route-generation-url 또는 AI_ROUTE_GENERATION_URL 환경 변수를 설정하세요.");
		}

		String url = baseUrl.endsWith("/") ? baseUrl + "generate" : baseUrl + "/generate";
		AiRouteRequestPayload.FilterPayload filterPayload = null;
		if (request.filter() != null) {
			RouteFilterRequest f = request.filter();
			filterPayload = new AiRouteRequestPayload.FilterPayload(
					f.activityLevel(),
					f.slopeLevel(),
					f.includeRestPoints(),
					f.natureFriendly(),
					f.pedestrianOnly());
		}
		AiRouteRequestPayload payload = new AiRouteRequestPayload(
				request.currentLatitude(),
				request.currentLongitude(),
				request.destinationIds() != null ? request.destinationIds() : java.util.List.of(),
				filterPayload);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<AiRouteRequestPayload> entity = new HttpEntity<>(payload, headers);

		ResponseEntity<AiRouteGenerationResponse> response = restTemplate.postForEntity(
				url,
				entity,
				AiRouteGenerationResponse.class);

		if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
			throw new IllegalStateException("AI 경로 생성 서비스 응답 오류: " + response.getStatusCode());
		}
		return response.getBody();
	}
}
