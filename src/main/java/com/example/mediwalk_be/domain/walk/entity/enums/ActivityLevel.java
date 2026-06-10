package com.example.mediwalk_be.domain.walk.entity.enums;

/**
 * 활동량 (맞춤형 산책 경로 Progress 1단계)
 * UI 카피: 약 2천 보 / 4천 보 / 6천 보
 * 목적지 선택 목표 Tmap 보행 거리: 약 500m / 1.2km / 2.5km (직선 추정은 우회 1.3배 보정)
 */
public enum ActivityLevel {
	MODERATE,   // 적당히 걷고 싶어요
	ACTIVE,     // 활발하게 걷고 싶어요
	MAXIMUM     // 최대로 걷고 싶어요
}
