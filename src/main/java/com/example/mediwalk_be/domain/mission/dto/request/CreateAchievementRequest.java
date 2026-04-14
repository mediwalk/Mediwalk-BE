package com.example.mediwalk_be.domain.mission.dto.request;

import com.example.mediwalk_be.domain.mission.entity.enums.AchievementCategory;

import jakarta.validation.constraints.NotBlank;

public record CreateAchievementRequest(
		@NotBlank String code,
		@NotBlank String name,
		String description,
		AchievementCategory category,
		Integer targetValue,
		String unit,
		String iconType
) {
}
