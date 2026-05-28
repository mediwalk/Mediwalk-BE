package com.example.mediwalk_be.domain.reward.dto.response;

import com.example.mediwalk_be.domain.reward.entity.Event;

public record EventCreateResponse(
		EventResponse event,
		Integer totalAccumulatedReward,
		Integer todayWalkingDistanceMeters,
		/** 오늘 달성한 성취명. 없으면 null (FE에서 "-" 표시) */
		String todayAchievementName
) {
	public static EventCreateResponse of(
			Event event,
			int totalAccumulatedReward,
			int todayWalkingDistanceMeters,
			String todayAchievementName) {
		return new EventCreateResponse(
				EventResponse.from(event),
				totalAccumulatedReward,
				todayWalkingDistanceMeters,
				todayAchievementName
		);
	}
}
