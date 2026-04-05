package com.example.mediwalk_be.dto.response;

import com.example.mediwalk_be.entity.CollectionLocation;
import com.example.mediwalk_be.entity.enums.CollectionLocationType;

import java.time.LocalDateTime;

public record CollectionLocationWithDistanceResponse(
	Long id,
	String name,
	String address,
	Double latitude,
	Double longitude,
	CollectionLocationType type,
	Integer baseRewardAmount,
	Integer activationRadius,
	Integer distanceMeters,
	Integer walkingDistanceMeters,
	// 예상 도보 시간 (분) - 현재는 직선거리 기반 추정
	Integer estimatedWalkTimeMinutes,
	// 예상 걸음 수 (평균 보폭 0.7m 기준)
	Integer estimatedSteps,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	private static final double DEFAULT_WALK_SPEED_METERS_PER_MINUTE = 80.0; // 약 4.8km/h

	public static CollectionLocationWithDistanceResponse from(
			CollectionLocation location,
			double currentLatitude,
			double currentLongitude) {
		// 거리 계산 (미터)
		double distanceMeters = com.example.mediwalk_be.util.DistanceUtil.calculateDistanceMeters(
				currentLatitude,
				currentLongitude,
				location.getLatitude(),
				location.getLongitude()
		);
		
		// 도보 거리는 직선 거리와 동일 (실제 도보 경로는 별도 계산 필요하지만 현재는 직선 거리 사용)
		int walkingDistanceMeters = (int) Math.round(distanceMeters);
		
		// 예상 도보 시간(분) - 직선거리 기반 추정
		int estimatedWalkTimeMinutes = Math.max(1, (int) Math.round(walkingDistanceMeters / DEFAULT_WALK_SPEED_METERS_PER_MINUTE));

		// 예상 걸음 수 계산
		int estimatedSteps = (int) Math.round(distanceMeters / 0.7);
		
		return new CollectionLocationWithDistanceResponse(
			location.getId(),
			location.getName(),
			location.getAddress(),
			location.getLatitude(),
			location.getLongitude(),
			location.getType(),
			location.getBaseRewardAmount(),
			location.getActivationRadius(),
			walkingDistanceMeters,
			walkingDistanceMeters,
			estimatedWalkTimeMinutes,
			estimatedSteps,
			location.getCreatedAt(),
			location.getUpdatedAt()
		);
	}
}
