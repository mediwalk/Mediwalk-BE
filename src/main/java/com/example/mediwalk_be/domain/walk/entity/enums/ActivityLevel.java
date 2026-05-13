package com.example.mediwalk_be.domain.walk.entity.enums;

/**
 * 활동량 (맞춤형 산책 경로 Progress 1단계)
 * 적당히: 약 2천 보 / 활발하게: 약 4천 보 / 최대: 약 6천 보
 */
public enum ActivityLevel {
	MODERATE,   // 적당히 걷고 싶어요
	ACTIVE,     // 활발하게 걷고 싶어요
	MAXIMUM     // 최대로 걷고 싶어요
}
