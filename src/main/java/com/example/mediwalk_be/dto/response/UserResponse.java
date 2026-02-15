package com.example.mediwalk_be.dto.response;

import com.example.mediwalk_be.entity.User;
import com.example.mediwalk_be.entity.enums.Gender;
import com.example.mediwalk_be.entity.enums.UserRole;
import com.example.mediwalk_be.entity.enums.UserStatus;

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
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static UserResponse from(User user) {
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
			user.getCreatedAt(),
			user.getUpdatedAt()
		);
	}
}
