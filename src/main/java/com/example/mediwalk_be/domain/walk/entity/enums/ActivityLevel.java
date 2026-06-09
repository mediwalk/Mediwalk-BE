package com.example.mediwalk_be.domain.walk.entity.enums;

/**
 * 활동량 (맞춤형 산책 경로 Progress 1단계)
 * UI 카피: 약 2천 보 / 4천 보 / 6천 보
 * 목적지 선택 목표 걸음(3km 이내): 2,000 / 3,000 / 4,000보
 */
public enum ActivityLevel {
	MODERATE,   // 적당히 걷고 싶어요
	ACTIVE,     // 활발하게 걷고 싶어요
	MAXIMUM     // 최대로 걷고 싶어요
}
