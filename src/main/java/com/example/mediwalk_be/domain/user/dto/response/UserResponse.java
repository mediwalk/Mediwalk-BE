package com.example.mediwalk_be.domain.user.dto.response;

import com.example.mediwalk_be.domain.user.entity.User;
import com.example.mediwalk_be.domain.user.entity.enums.Gender;
import com.example.mediwalk_be.domain.user.entity.enums.UserRole;
import com.example.mediwalk_be.domain.user.entity.enums.UserStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserResponse(
	Long id,
	String email,
	String name,
	String phone,
	LocalDate birthDate,
	Gender gender,
	UserRole role,
	UserStatus status,
	Integer totalAccumulatedReward,
	Integer totalCollectionsCount,
	Double currentLatitude,
	Double currentLongitude,
	/** 지난 달 적립 리워드 합계 (원) - 홈 화면용 */
	Integer lastMonthRewardTotal,
	/** 이번 달 적립 리워드 합계 (원) - 홈 화면용 */
	Integer thisMonthRewardTotal,
	/** 지난 달 대비 리워드 증가율 (%) - 지난 달이 0원이면 null */
	Double rewardIncreaseRateComparedToLastMonth,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static UserResponse from(User user) {
		return from(user, null, null, null);
	}

	public static UserResponse from(User user, Integer lastMonthRewardTotal, Integer thisMonthRewardTotal, Double rewardIncreaseRateComparedToLastMonth) {
		return new UserResponse(
			user.getId(),
			user.getEmail(),
			user.getName(),
			user.getPhone(),
			user.getBirthDate(),
			user.getGender(),
			user.getRole(),
			user.getStatus(),
			user.getTotalAccumulatedReward(),
			user.getTotalCollectionsCount(),
			user.getCurrentLatitude(),
			user.getCurrentLongitude(),
			lastMonthRewardTotal,
			thisMonthRewardTotal,
			rewardIncreaseRateComparedToLastMonth,
			user.getCreatedAt(),
			user.getUpdatedAt()
		);
	}
}
