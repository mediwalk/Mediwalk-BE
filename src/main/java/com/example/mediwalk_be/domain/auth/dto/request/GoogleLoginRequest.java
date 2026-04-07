package com.example.mediwalk_be.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
		@NotBlank(message = "Firebase ID 토큰이 필요합니다.")
		String idToken
) {
}
