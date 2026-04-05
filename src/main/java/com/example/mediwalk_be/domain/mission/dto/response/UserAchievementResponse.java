package com.example.mediwalk_be.domain.mission.dto.response;

import com.example.mediwalk_be.domain.mission.entity.UserAchievement;

import java.time.LocalDateTime;

public record UserAchievementResponse(
	Long id,
	Long userId,
	Long achievementId,
	Integer currentProgress,
	Boolean isAchieved,
	LocalDateTime achievedDate,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static UserAchievementResponse from(UserAchievement e) {
		return new UserAchievementResponse(
			e.getId(),
			e.getUser().getId(),
			e.getAchievement().getId(),
			e.getCurrentProgress(),
			e.getIsAchieved(),
			e.getAchievedDate(),
			e.getCreatedAt(),
			e.getUpdatedAt()
		);
	}
}
