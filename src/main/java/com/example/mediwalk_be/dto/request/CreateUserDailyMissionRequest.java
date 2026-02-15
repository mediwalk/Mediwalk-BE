package com.example.mediwalk_be.dto.request;

import java.time.LocalDate;

public record CreateUserDailyMissionRequest(
	Long userId,
	Long missionId,
	Long collectionLocationId,
	LocalDate missionDate
) {
}
