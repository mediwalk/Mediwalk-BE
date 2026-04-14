package com.example.mediwalk_be.domain.reward.dto.response;

import com.example.mediwalk_be.domain.mission.entity.UserAchievement;
import com.example.mediwalk_be.domain.mission.entity.enums.AchievementCategory;

import java.time.LocalDateTime;

public record RewardMainAchievementResponse(
		Long userAchievementId,
		Long achievementId,
		String achievementCode,
		String achievementName,
		String achievementDescription,
		AchievementCategory achievementCategory,
		Boolean isAchieved,
		LocalDateTime achievedDate,
		String iconType
) {
	public static RewardMainAchievementResponse from(UserAchievement e) {
		return new RewardMainAchievementResponse(
				e.getId(),
				e.getAchievement().getId(),
				e.getAchievement().getCode(),
				e.getAchievement().getName(),
				e.getAchievement().getDescription(),
				e.getAchievement().getCategory(),
				e.getIsAchieved(),
				e.getAchievedDate(),
				e.getAchievement().getIconType()
		);
	}
}
