package com.example.mediwalk_be.domain.reward.dto.response;

public record ConsumableCategoryResponse(
		String code,
		String displayName,
		int sortOrder,
		long itemCount
) {
}
