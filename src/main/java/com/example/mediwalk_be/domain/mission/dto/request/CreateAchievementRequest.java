package com.example.mediwalk_be.domain.mission.dto.request;

import com.example.mediwalk_be.domain.mission.entity.enums.AchievementCategory;

public record CreateAchievementRequest(
	String name,
	String description,
	AchievementCategory category,
	Integer targetValue,
	String unit,
	String iconType
) {
}
