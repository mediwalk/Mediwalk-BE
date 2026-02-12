package com.example.mediwalk_be.entity.enums;

/**
 * 활동량 기준 (AI 맞춤 경로 필터)
 * 적당한: 약 3,000~5,000보 / 활발한: 약 7,000~10,000보 / 최대의: 10,000보 초과
 */
public enum ActivityLevel {
	MODERATE,   // 적당한
	ACTIVE,     // 활발한
	MAXIMUM     // 최대의
}
