package com.example.mediwalk_be.domain.mission.dto.request;

import java.time.LocalDate;

public record CreateUserDailyMissionRequest(
	Long missionId,
	Long collectionLocationId,
	LocalDate missionDate
) {
}
