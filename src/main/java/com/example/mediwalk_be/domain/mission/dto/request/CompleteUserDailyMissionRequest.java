package com.example.mediwalk_be.domain.mission.dto.request;

public record CompleteUserDailyMissionRequest(
	Integer earnedReward,
	Double currentLatitude,
	Double currentLongitude
) {
}
