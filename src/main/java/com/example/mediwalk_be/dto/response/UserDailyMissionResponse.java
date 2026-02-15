package com.example.mediwalk_be.dto.response;

import com.example.mediwalk_be.entity.UserDailyMission;
import com.example.mediwalk_be.entity.enums.MissionStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserDailyMissionResponse(
	Long id,
	Long userId,
	Long missionId,
	Long collectionLocationId,
	LocalDate missionDate,
	MissionStatus status,
	LocalDateTime completedAt,
	Integer earnedReward,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static UserDailyMissionResponse from(UserDailyMission e) {
		return new UserDailyMissionResponse(
			e.getId(),
			e.getUser().getId(),
			e.getMission().getId(),
			e.getCollectionLocation() != null ? e.getCollectionLocation().getId() : null,
			e.getMissionDate(),
			e.getStatus(),
			e.getCompletedAt(),
			e.getEarnedReward(),
			e.getCreatedAt(),
			e.getUpdatedAt()
		);
	}
}
