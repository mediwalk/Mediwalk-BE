package com.example.mediwalk_be.domain.reward.dto.request;

import com.example.mediwalk_be.domain.reward.entity.enums.EventType;

import java.time.LocalDateTime;

public record CreateEventRequest(
	Long userId,
	EventType eventType,
	String title,
	Integer rewardAmount,
	LocalDateTime eventDateTime,
	String locationName,
	Long collectionLocationId,
	String imageUrl,
	Long routeId,
	Long userDailyMissionId,
	Double currentLatitude,
	Double currentLongitude
) {
}
