package com.example.mediwalk_be.domain.reward.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 소모품 지원 신청 화면 카테고리 (화면설계서 Detail Description 기준 순서).
 * "전체" 탭은 코드 없이 필터 미적용으로 처리.
 */
@Getter
@RequiredArgsConstructor
public enum ConsumableCategoryCode {

	BLOOD_GLUCOSE(0, "혈당 측정"),
	INSULIN(1, "인슐린 투여"),
	CONTINUOUS_GLUCOSE(2, "연속 혈당"),
	HEALTH_FUNCTION(3, "건강 기능"),
	HYPOGLYCEMIA_PREP(4, "저혈당 대비");

	private final int sortOrder;
	private final String displayName;
}
