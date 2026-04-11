package com.example.mediwalk_be.domain.reward.dto.response;

import com.example.mediwalk_be.domain.reward.entity.ConsumableItem;

public record ConsumableItemResponse(
		Long id,
		String categoryCode,
		String categoryDisplayName,
		String name,
		Integer priceWon,
		int sortOrder,
		String imageUrl
) {
	public static ConsumableItemResponse from(ConsumableItem e) {
		return new ConsumableItemResponse(
				e.getId(),
				e.getCategoryCode().name(),
				e.getCategoryCode().getDisplayName(),
				e.getName(),
				e.getPriceWon(),
				e.getSortOrder(),
				e.getImageUrl()
		);
	}
}
