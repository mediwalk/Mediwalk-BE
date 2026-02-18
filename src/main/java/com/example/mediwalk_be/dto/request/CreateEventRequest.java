package com.example.mediwalk_be.dto.request;

import com.example.mediwalk_be.entity.enums.EventType;

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
	Long routeId
) {
}
