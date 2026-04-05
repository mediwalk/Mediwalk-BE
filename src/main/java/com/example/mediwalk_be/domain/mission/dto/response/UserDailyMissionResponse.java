package com.example.mediwalk_be.domain.mission.dto.response;

import com.example.mediwalk_be.domain.mission.entity.UserDailyMission;
import com.example.mediwalk_be.domain.mission.entity.enums.MissionStatus;

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
	String missionTitle,
	String missionDescription,
	Integer distanceMeters,
	Integer walkingDistanceMeters,
	Integer estimatedWalkTimeMinutes,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	private static final double DEFAULT_WALK_SPEED_METERS_PER_MINUTE = 80.0; // 약 4.8km/h

	public static UserDailyMissionResponse from(UserDailyMission e) {
		return from(e, null, null);
	}

	public static UserDailyMissionResponse from(UserDailyMission e, Double currentLatitude, Double currentLongitude) {
		// 미션 정보
		String missionTitle = e.getMission().getTitle();
		String missionDescription = e.getMission().getDescription();

		// 거리 계산
		Integer distanceMeters = null;
		Integer walkingDistanceMeters = null;
		Integer estimatedWalkTimeMinutes = null;
		if (currentLatitude != null && currentLongitude != null && e.getCollectionLocation() != null) {
			double distance = com.example.mediwalk_be.domain.walk.util.DistanceUtil.calculateDistanceMeters(
					currentLatitude,
					currentLongitude,
					e.getCollectionLocation().getLatitude(),
					e.getCollectionLocation().getLongitude()
			);
			distanceMeters = (int) Math.round(distance);
			walkingDistanceMeters = distanceMeters;
			estimatedWalkTimeMinutes = Math.max(1, (int) Math.round(walkingDistanceMeters / DEFAULT_WALK_SPEED_METERS_PER_MINUTE));
		}

		return new UserDailyMissionResponse(
			e.getId(),
			e.getUser().getId(),
			e.getMission().getId(),
			e.getCollectionLocation() != null ? e.getCollectionLocation().getId() : null,
			e.getMissionDate(),
			e.getStatus(),
			e.getCompletedAt(),
			e.getEarnedReward(),
			missionTitle,
			missionDescription,
			distanceMeters,
			walkingDistanceMeters,
			estimatedWalkTimeMinutes,
			e.getCreatedAt(),
			e.getUpdatedAt()
		);
	}
}
