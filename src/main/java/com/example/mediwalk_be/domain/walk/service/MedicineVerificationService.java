package com.example.mediwalk_be.domain.walk.service;

import com.example.mediwalk_be.domain.walk.client.MedicineVerificationClient;
import com.example.mediwalk_be.domain.walk.client.dto.AiVerificationPrediction;
import com.example.mediwalk_be.domain.walk.dto.response.MedicineVerificationResponse;
import com.example.mediwalk_be.domain.walk.dto.response.MedicineVerificationResponse.VerificationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicineVerificationService {

	private static final String PILL_LABEL = "pill";
	private static final double VERIFIED_THRESHOLD = 0.7;
	private static final double RETRY_THRESHOLD = 0.5;

	private final MedicineVerificationClient medicineVerificationClient;

	public MedicineVerificationResponse verify(String base64Image) {
		List<AiVerificationPrediction> predictions = medicineVerificationClient.verify(base64Image);

		if (predictions == null || predictions.isEmpty()) {
			return new MedicineVerificationResponse(
					VerificationStatus.FAILED, 0.0, 0, "폐의약품을 인식하지 못했습니다. 다시 시도해 주세요.");
		}

		double pillConfidence = predictions.stream()
				.mapToDouble(this::extractPillConfidence)
				.max()
				.orElse(0.0);

		int detectedCount = pillConfidence >= RETRY_THRESHOLD ? 1 : 0;

		return new MedicineVerificationResponse(
				evaluateStatus(pillConfidence),
				pillConfidence,
				detectedCount,
				buildMessage(pillConfidence));
	}

	/**
	 * Vertex 분류 모델 응답에서 {@code displayNames}와 {@code confidences}를 짝지어
	 * {@code pill} 라벨 confidence만 사용한다. (no_pill 등 다른 클래스 max는 사용하지 않음)
	 */
	private double extractPillConfidence(AiVerificationPrediction prediction) {
		List<String> displayNames = prediction.displayNames();
		List<Double> confidences = prediction.confidences();
		if (displayNames == null || confidences == null || displayNames.isEmpty()) {
			return 0.0;
		}

		double best = 0.0;
		int limit = Math.min(displayNames.size(), confidences.size());
		for (int i = 0; i < limit; i++) {
			String name = displayNames.get(i);
			if (name != null && PILL_LABEL.equalsIgnoreCase(name.trim())) {
				Double confidence = confidences.get(i);
				if (confidence != null) {
					best = Math.max(best, confidence);
				}
			}
		}
		return best;
	}

	private VerificationStatus evaluateStatus(double pillConfidence) {
		if (pillConfidence >= VERIFIED_THRESHOLD) return VerificationStatus.VERIFIED;
		if (pillConfidence >= RETRY_THRESHOLD) return VerificationStatus.RETRY;
		return VerificationStatus.FAILED;
	}

	private String buildMessage(double pillConfidence) {
		if (pillConfidence >= VERIFIED_THRESHOLD) return "폐의약품이 확인되었습니다.";
		if (pillConfidence >= RETRY_THRESHOLD) return "사진이 불분명합니다. 더 가까이서 다시 촬영해 주세요.";
		return "폐의약품을 인식하지 못했습니다. 다시 시도해 주세요.";
	}
}
