package com.example.mediwalk_be.dto.response;

import com.example.mediwalk_be.entity.DailySteps;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DailyStepsResponse(
	Long id,
	Long userId,
	LocalDate date,
	Integer stepsCount,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static DailyStepsResponse from(DailySteps e) {
		return new DailyStepsResponse(
			e.getId(),
			e.getUser().getId(),
			e.getDate(),
			e.getStepsCount(),
			e.getCreatedAt(),
			e.getUpdatedAt()
		);
	}
}
