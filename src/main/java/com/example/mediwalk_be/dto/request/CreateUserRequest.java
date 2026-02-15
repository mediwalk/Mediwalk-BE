package com.example.mediwalk_be.dto.request;

import com.example.mediwalk_be.entity.enums.Gender;
import com.example.mediwalk_be.entity.enums.UserRole;
import com.example.mediwalk_be.entity.enums.UserStatus;

import java.time.LocalDate;

public record CreateUserRequest(
	String email,
	String password,
	String name,
	String phone,
	LocalDate birthDate,
	Gender gender,
	UserRole role,
	UserStatus status
) {
}
