package com.example.mediwalk_be.domain.walk.dto.response;

public record MedicineVerificationResponse(
		VerificationStatus status,
		double maxConfidence,
		int detectedCount,
		String message
) {
	public enum VerificationStatus {
		VERIFIED,  // 0.7 이상 — 검출 인정
		RETRY,     // 0.5 ~ 0.7 — 재촬영 요청
		FAILED     // 0.5 미만 또는 미탐지 — 실패
	}
}
