package com.example.mediwalk_be.domain.mission.dto.request;

import com.example.mediwalk_be.domain.mission.entity.enums.MissionType;

public record CreateMissionRequest(
	MissionType missionType,
	String title,
	String description,
	Integer baseRewardAmount
) {
}
