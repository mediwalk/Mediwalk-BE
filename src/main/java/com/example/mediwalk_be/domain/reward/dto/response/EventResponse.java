package com.example.mediwalk_be.domain.reward.dto.response;

import com.example.mediwalk_be.domain.reward.entity.Event;
import com.example.mediwalk_be.domain.reward.entity.enums.EventType;

import java.time.LocalDateTime;

public record EventResponse(
	Long id,
	Long userId,
	EventType eventType,
	String title,
	Integer rewardAmount,
	LocalDateTime eventDateTime,
	String locationName,
	Long collectionLocationId,
	String imageUrl,
	Long routeId,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static EventResponse from(Event e) {
		return new EventResponse(
			e.getId(),
			e.getUser().getId(),
			e.getEventType(),
			e.getTitle(),
			e.getRewardAmount(),
			e.getEventDateTime(),
			e.getLocationName(),
			e.getCollectionLocation() != null ? e.getCollectionLocation().getId() : null,
			e.getImageUrl(),
			e.getRoute() != null ? e.getRoute().getId() : null,
			e.getCreatedAt(),
			e.getUpdatedAt()
		);
	}
}
