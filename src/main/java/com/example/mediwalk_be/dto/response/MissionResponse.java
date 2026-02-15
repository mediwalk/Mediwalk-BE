package com.example.mediwalk_be.dto.response;

import com.example.mediwalk_be.entity.Mission;
import com.example.mediwalk_be.entity.enums.MissionType;

import java.time.LocalDateTime;

public record MissionResponse(
	Long id,
	MissionType missionType,
	String title,
	String description,
	Integer baseRewardAmount,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static MissionResponse from(Mission e) {
		return new MissionResponse(
			e.getId(),
			e.getMissionType(),
			e.getTitle(),
			e.getDescription(),
			e.getBaseRewardAmount(),
			e.getCreatedAt(),
			e.getUpdatedAt()
		);
	}
}
