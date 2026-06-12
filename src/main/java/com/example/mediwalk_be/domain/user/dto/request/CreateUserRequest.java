package com.example.mediwalk_be.domain.user.dto.request;

import com.example.mediwalk_be.domain.user.entity.enums.Gender;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record CreateUserRequest(
	@NotBlank @Email String email,
	@NotBlank String password,
	@NotBlank String name,
	String phone,
	LocalDate birthDate,
	Gender gender
) {
}
