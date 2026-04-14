package com.example.mediwalk_be.domain.reward.controller;

import com.example.mediwalk_be.domain.reward.dto.response.ConsumableCategoryResponse;
import com.example.mediwalk_be.domain.reward.dto.response.ConsumableItemResponse;
import com.example.mediwalk_be.domain.reward.entity.enums.ConsumableCategoryCode;
import com.example.mediwalk_be.domain.reward.service.ConsumableCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/consumables")
@RequiredArgsConstructor
@Tag(name = "Reward", description = "리워드 도메인: 이벤트(수거·미션), 거래(적립·환급), 요약")
public class ConsumableController {

	private final ConsumableCatalogService consumableCatalogService;

	@GetMapping("/categories")
	@Operation(summary = "소모품 카테고리 목록", description = "화면설계서 Detail Description 기준 카테고리(혈당 측정, 인슐린 투여, 연속 혈당, 건강 기능, 저혈당 대비)와 각 카테고리별 활성 상품 수를 반환합니다. '전체' 탭은 카테고리 필터 없이 상품 목록 API를 사용하세요.")
	public List<ConsumableCategoryResponse> listCategories() {
		return consumableCatalogService.listCategories();
	}

	@GetMapping("/items")
	@Operation(summary = "소모품 상품 목록", description = "categoryCode 미지정 시 전체(설계서 '전체' 탭). 지정 시 해당 카테고리만. 정렬은 카테고리 순서 → 상품 나열 순입니다.")
	public List<ConsumableItemResponse> listItems(
			@Parameter(description = "ConsumableCategoryCode enum 이름. 예: BLOOD_GLUCOSE. 생략 시 전체.")
			@RequestParam(required = false) String categoryCode) {
		Optional<ConsumableCategoryCode> code = parseCategoryCode(categoryCode);
		return consumableCatalogService.listItems(code);
	}

	private static Optional<ConsumableCategoryCode> parseCategoryCode(String raw) {
		if (raw == null || raw.isBlank()) {
			return Optional.empty();
		}
		try {
			return Optional.of(ConsumableCategoryCode.valueOf(raw.trim()));
		} catch (IllegalArgumentException ex) {
			throw new IllegalArgumentException("유효하지 않은 categoryCode: " + raw);
		}
	}
}
