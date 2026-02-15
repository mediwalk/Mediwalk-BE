package com.example.mediwalk_be.dto.request;

import com.example.mediwalk_be.entity.enums.MissionType;

public record CreateMissionRequest(
	MissionType missionType,
	String title,
	String description,
	Integer baseRewardAmount
) {
}
