package com.example.mediwalk_be.dto.request;

public record CompleteUserDailyMissionRequest(
	Integer earnedReward,
	Double currentLatitude,
	Double currentLongitude
) {
}
