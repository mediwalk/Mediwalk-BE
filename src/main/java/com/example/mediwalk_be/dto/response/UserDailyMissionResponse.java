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
	/** 미션 제목 */
	String missionTitle,
	/** 미션 설명 */
	String missionDescription,
	/** 목적지까지의 거리 (미터) - 9번: 현재 위치가 제공된 경우에만 값이 있음 */
	Integer distanceMeters,
	/** 목적지까지의 도보 거리 (미터) - 9번: 현재 위치가 제공된 경우에만 값이 있음 */
	Integer walkingDistanceMeters,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static UserDailyMissionResponse from(UserDailyMission e) {
		return from(e, null, null);
	}

	public static UserDailyMissionResponse from(UserDailyMission e, Double currentLatitude, Double currentLongitude) {
		// 미션 정보
		String missionTitle = e.getMission().getTitle();
		String missionDescription = e.getMission().getDescription();

		// 거리 계산 (9번: 현재 위치가 제공되고 목적지가 있는 경우)
		Integer distanceMeters = null;
		Integer walkingDistanceMeters = null;
		if (currentLatitude != null && currentLongitude != null && e.getCollectionLocation() != null) {
			double distance = com.example.mediwalk_be.util.DistanceUtil.calculateDistanceMeters(
					currentLatitude,
					currentLongitude,
					e.getCollectionLocation().getLatitude(),
					e.getCollectionLocation().getLongitude()
			);
			distanceMeters = (int) Math.round(distance);
			walkingDistanceMeters = distanceMeters;
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
			e.getCreatedAt(),
			e.getUpdatedAt()
		);
	}
}
