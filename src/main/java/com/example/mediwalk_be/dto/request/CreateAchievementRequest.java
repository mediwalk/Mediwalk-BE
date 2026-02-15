package com.example.mediwalk_be.dto.request;

import com.example.mediwalk_be.entity.enums.AchievementCategory;

public record CreateAchievementRequest(
	String name,
	String description,
	AchievementCategory category,
	Integer targetValue,
	String unit,
	String iconType
) {
}
