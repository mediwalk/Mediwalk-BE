package com.example.mediwalk_be.domain.walk.entity.enums;

/**
 * 경사도 기준 (AI 맞춤 경로 필터)
 * 완만한: 0~3% / 적당한: 4~7% / 가파른: 8~12%+
 */
public enum SlopeLevel {
	GENTLE,     // 완만한
	MODERATE,   // 적당한
	STEEP       // 가파른
}
