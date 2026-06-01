package com.example.mediwalk_be.domain.walk.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MedicineVerificationRequest(
		@NotBlank String base64Image
) {}
