package com.example.mediwalk_be.domain.mission.dto.response;

import com.example.mediwalk_be.domain.mission.entity.Achievement;
import com.example.mediwalk_be.domain.mission.entity.enums.AchievementCategory;

import java.time.LocalDateTime;

public record AchievementResponse(
	Long id,
	String name,
	String description,
	AchievementCategory category,
	Integer targetValue,
	String unit,
	String iconType,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static AchievementResponse from(Achievement e) {
		return new AchievementResponse(
			e.getId(),
			e.getName(),
			e.getDescription(),
			e.getCategory(),
			e.getTargetValue(),
			e.getUnit(),
			e.getIconType(),
			e.getCreatedAt(),
			e.getUpdatedAt()
		);
	}
}
