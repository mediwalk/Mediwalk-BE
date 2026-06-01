package com.example.mediwalk_be.domain.walk.controller;

import com.example.mediwalk_be.domain.walk.dto.request.MedicineVerificationRequest;
import com.example.mediwalk_be.domain.walk.dto.response.MedicineVerificationResponse;
import com.example.mediwalk_be.domain.walk.service.MedicineVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/medicine-verifications")
@RequiredArgsConstructor
@Tag(name = "MedicineVerification", description = "폐의약품 인증: Vertex AI 기반 이미지 검증")
public class MedicineVerificationController {

	private final MedicineVerificationService medicineVerificationService;

	@PostMapping
	@Operation(
			summary = "폐의약품 인증",
			description = """
					base64 인코딩된 이미지를 Vertex AI에 전송하여 폐의약품 여부를 판정합니다.
					pill 라벨 confidence 기준 (no_pill 등 다른 클래스 점수는 사용하지 않음).
					- VERIFIED: pill 신뢰도 0.7 이상 — 인증 성공
					- RETRY: pill 신뢰도 0.5~0.7 — 재촬영 요청
					- FAILED: pill 신뢰도 0.5 미만 또는 미탐지 — 인증 실패
					"""
	)
	public ResponseEntity<MedicineVerificationResponse> verify(
			@Valid @RequestBody MedicineVerificationRequest request) {
		return ResponseEntity.ok(medicineVerificationService.verify(request.base64Image()));
	}
}
