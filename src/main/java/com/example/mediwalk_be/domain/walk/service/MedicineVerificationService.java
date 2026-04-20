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

	private static final double VERIFIED_THRESHOLD = 0.7;
	private static final double RETRY_THRESHOLD = 0.5;

	private final MedicineVerificationClient medicineVerificationClient;

	public MedicineVerificationResponse verify(String base64Image) {
		List<AiVerificationPrediction> predictions = medicineVerificationClient.verify(base64Image);

		if (predictions == null || predictions.isEmpty()) {
			return new MedicineVerificationResponse(
					VerificationStatus.FAILED, 0.0, 0, "폐의약품을 인식하지 못했습니다. 다시 시도해 주세요.");
		}

		double maxConfidence = predictions.stream()
				.flatMap(p -> p.confidences() != null ? p.confidences().stream() : java.util.stream.Stream.empty())
				.mapToDouble(Double::doubleValue)
				.max()
				.orElse(0.0);

		int detectedCount = predictions.stream()
				.mapToInt(p -> p.confidences() != null ? p.confidences().size() : 0)
				.sum();

		return new MedicineVerificationResponse(
				evaluateStatus(maxConfidence),
				maxConfidence,
				detectedCount,
				buildMessage(maxConfidence));
	}

	private VerificationStatus evaluateStatus(double maxConfidence) {
		if (maxConfidence >= VERIFIED_THRESHOLD) return VerificationStatus.VERIFIED;
		if (maxConfidence >= RETRY_THRESHOLD)   return VerificationStatus.RETRY;
		return VerificationStatus.FAILED;
	}

	private String buildMessage(double maxConfidence) {
		if (maxConfidence >= VERIFIED_THRESHOLD) return "폐의약품이 확인되었습니다.";
		if (maxConfidence >= RETRY_THRESHOLD)    return "사진이 불분명합니다. 더 가까이서 다시 촬영해 주세요.";
		return "폐의약품을 인식하지 못했습니다. 다시 시도해 주세요.";
	}
}
