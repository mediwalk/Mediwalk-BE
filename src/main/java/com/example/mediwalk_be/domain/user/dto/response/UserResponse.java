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
	String profileImageUrl,
	String phone,
	LocalDate birthDate,
	Gender gender,
	UserRole role,
	UserStatus status,
	Integer totalAccumulatedReward,
	Integer totalCollectionsCount,
	Double currentLatitude,
	Double currentLongitude,
	/** 이번 달 폐의약품 수거 적립 합계 (원) - 홈 화면 카드용 */
	Integer thisMonthMedicineCollectionRewardTotal,
	/** 지난 달 대비 폐의약품 수거 적립 증가율 (%) - 홈 화면 카드용. 지난 달 0원이면 null */
	Double medicineCollectionRewardIncreaseRateComparedToLastMonth,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static UserResponse from(User user) {
		return from(user, null, null);
	}

	public static UserResponse from(User user, Integer thisMonthMedicineCollectionRewardTotal, Double medicineCollectionRewardIncreaseRateComparedToLastMonth) {
		return new UserResponse(
			user.getId(),
			user.getEmail(),
			user.getName(),
			user.getProfileImageUrl(),
			user.getPhone(),
			user.getBirthDate(),
			user.getGender(),
			user.getRole(),
			user.getStatus(),
			user.getTotalAccumulatedReward(),
			user.getTotalCollectionsCount(),
			user.getCurrentLatitude(),
			user.getCurrentLongitude(),
			thisMonthMedicineCollectionRewardTotal,
			medicineCollectionRewardIncreaseRateComparedToLastMonth,
			user.getCreatedAt(),
			user.getUpdatedAt()
		);
	}
}
