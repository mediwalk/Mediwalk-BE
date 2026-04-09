package com.example.mediwalk_be.domain.reward.dto.response;

import com.example.mediwalk_be.domain.mission.entity.UserAchievement;
import com.example.mediwalk_be.domain.mission.entity.enums.AchievementCategory;

import java.time.LocalDateTime;

public record RewardMainAchievementResponse(
	Long userAchievementId,
	Long achievementId,
	String achievementName,
	String achievementDescription,
	AchievementCategory achievementCategory,
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
	public static RewardMainAchievementResponse from(UserAchievement e) {
		Integer targetValue = e.getAchievement().getTargetValue();
		int progressPercent = 0;
		if (targetValue != null && targetValue > 0) {
			progressPercent = (int) Math.min(100, Math.round(((double) e.getCurrentProgress() / targetValue) * 100));
		}

		return new RewardMainAchievementResponse(
				e.getId(),
				e.getAchievement().getId(),
				e.getAchievement().getName(),
				e.getAchievement().getDescription(),
				e.getAchievement().getCategory(),
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
