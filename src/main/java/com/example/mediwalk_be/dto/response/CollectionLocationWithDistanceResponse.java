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
	// 현재 위치로부터의 거리 (미터)
	Integer distanceMeters,
	// 도보 거리 (미터)
	Integer walkingDistanceMeters,
	// 예상 걸음 수 (평균 보폭 0.7m 기준)
	Integer estimatedSteps,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
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
			estimatedSteps,
			location.getCreatedAt(),
			location.getUpdatedAt()
		);
	}
}
