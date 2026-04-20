package com.example.mediwalk_be.domain.walk.client;

import com.example.mediwalk_be.domain.walk.client.dto.AiVerificationPrediction;
import com.example.mediwalk_be.domain.walk.client.dto.AiVerificationRequestPayload;
import com.example.mediwalk_be.domain.walk.client.dto.AiVerificationResponse;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class MedicineVerificationClientImpl implements MedicineVerificationClient {

	private static final String VERTEX_AI_SCOPE = "https://www.googleapis.com/auth/cloud-platform";
	private static final long MAX_IMAGE_BYTES = (long) (1.5 * 1024 * 1024);

	private final RestTemplate restTemplate;
	private final String projectId;
	private final String endpointId;
	private final String region;
	private final String credentialsPath;

	public MedicineVerificationClientImpl(
			RestTemplate restTemplate,
			@Value("${app.ai.medicine-verification.project-id:}") String projectId,
			@Value("${app.ai.medicine-verification.endpoint-id:}") String endpointId,
			@Value("${app.ai.medicine-verification.region:us-central1}") String region,
			@Value("${app.ai.medicine-verification.credentials-path:}") String credentialsPath) {
		this.restTemplate = restTemplate;
		this.projectId = projectId;
		this.endpointId = endpointId;
		this.region = region;
		this.credentialsPath = credentialsPath;
	}

	@Override
	public List<AiVerificationPrediction> verify(String base64Image) {
		validateConfig();
		validateImageSize(base64Image);

		String url = buildUrl();
		String bearerToken = getBearerToken();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(bearerToken);

		HttpEntity<AiVerificationRequestPayload> entity =
				new HttpEntity<>(AiVerificationRequestPayload.of(base64Image), headers);

		ResponseEntity<AiVerificationResponse> response = restTemplate.postForEntity(
				url, entity, AiVerificationResponse.class);

		if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
			throw new IllegalStateException("Vertex AI 응답 오류: " + response.getStatusCode());
		}

		List<AiVerificationPrediction> predictions = response.getBody().predictions();
		return predictions != null ? predictions : Collections.emptyList();
	}

	private String buildUrl() {
		return String.format(
				"https://%s-aiplatform.googleapis.com/v1/projects/%s/locations/%s/endpoints/%s:predict",
				region, projectId, region, endpointId);
	}

	private String getBearerToken() {
		try {
			GoogleCredentials credentials = credentialsPath != null && !credentialsPath.isBlank()
					? GoogleCredentials.fromStream(new FileInputStream(credentialsPath))
							.createScoped(VERTEX_AI_SCOPE)
					: GoogleCredentials.getApplicationDefault()
							.createScoped(VERTEX_AI_SCOPE);
			credentials.refreshIfExpired();
			return credentials.getAccessToken().getTokenValue();
		} catch (IOException e) {
			throw new IllegalStateException("GCP 서비스 계정 인증 실패: " + e.getMessage(), e);
		}
	}

	private void validateConfig() {
		if (projectId == null || projectId.isBlank()) {
			throw new IllegalStateException("GCP_PROJECT_ID가 설정되지 않았습니다.");
		}
		if (endpointId == null || endpointId.isBlank()) {
			throw new IllegalStateException("GCP_ENDPOINT_ID가 설정되지 않았습니다.");
		}
	}

	private void validateImageSize(String base64Image) {
		// base64 문자 수 * 0.75 ≈ 원본 바이트 수
		long estimatedBytes = (long) (base64Image.length() * 0.75);
		if (estimatedBytes > MAX_IMAGE_BYTES) {
			throw new IllegalArgumentException(
					String.format("이미지 크기는 1.5MB 이하여야 합니다. 현재 약 %.1fMB",
							estimatedBytes / (1024.0 * 1024.0)));
		}
	}
}
