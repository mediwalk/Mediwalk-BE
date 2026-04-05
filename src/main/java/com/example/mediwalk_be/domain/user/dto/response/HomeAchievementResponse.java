package com.example.mediwalk_be.domain.user.dto.response;

import com.example.mediwalk_be.domain.mission.entity.UserAchievement;

import java.time.LocalDateTime;

public record HomeAchievementResponse(
	Long userAchievementId,
	Long achievementId,
	String achievementName,
	Integer currentProgress,
	Integer targetValue,
	String unit,
	Boolean isAchieved,
	LocalDateTime achievedDate,
	String iconType,
	Integer progressPercent,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static HomeAchievementResponse from(UserAchievement e) {
		Integer targetValue = e.getAchievement().getTargetValue();
		int progressPercent = 0;
		if (targetValue != null && targetValue > 0) {
			progressPercent = (int) Math.min(100, Math.round(((double) e.getCurrentProgress() / targetValue) * 100));
		}

		return new HomeAchievementResponse(
				e.getId(),
				e.getAchievement().getId(),
				e.getAchievement().getName(),
				e.getCurrentProgress(),
				e.getAchievement().getTargetValue(),
				e.getAchievement().getUnit(),
				e.getIsAchieved(),
				e.getAchievedDate(),
				e.getAchievement().getIconType(),
				progressPercent,
				e.getCreatedAt(),
				e.getUpdatedAt()
		);
	}
}

