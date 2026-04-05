package com.example.mediwalk_be.domain.walk.dto.response;

import com.example.mediwalk_be.domain.walk.entity.CollectionLocation;
import com.example.mediwalk_be.domain.walk.entity.enums.CollectionLocationType;

import java.time.LocalDateTime;

public record CollectionLocationResponse(
	Long id,
	String name,
	String address,
	Double latitude,
	Double longitude,
	CollectionLocationType type,
	Integer baseRewardAmount,
	Integer activationRadius,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static CollectionLocationResponse from(CollectionLocation e) {
		return new CollectionLocationResponse(
			e.getId(),
			e.getName(),
			e.getAddress(),
			e.getLatitude(),
			e.getLongitude(),
			e.getType(),
			e.getBaseRewardAmount(),
			e.getActivationRadius(),
			e.getCreatedAt(),
			e.getUpdatedAt()
		);
	}
}
