package com.example.mediwalk_be.domain.reward.service;

import com.example.mediwalk_be.domain.reward.dto.response.ConsumableCategoryResponse;
import com.example.mediwalk_be.domain.reward.dto.response.ConsumableItemResponse;
import com.example.mediwalk_be.domain.reward.entity.ConsumableItem;
import com.example.mediwalk_be.domain.reward.entity.enums.ConsumableCategoryCode;
import com.example.mediwalk_be.domain.reward.repository.ConsumableItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsumableCatalogService {

	private final ConsumableItemRepository consumableItemRepository;

	public List<ConsumableCategoryResponse> listCategories() {
		return Arrays.stream(ConsumableCategoryCode.values())
				.sorted(Comparator.comparingInt(ConsumableCategoryCode::getSortOrder))
				.map(code -> new ConsumableCategoryResponse(
						code.name(),
						code.getDisplayName(),
						code.getSortOrder(),
						consumableItemRepository.countByCategoryCodeAndActiveIsTrue(code)
				))
				.toList();
	}

	public List<ConsumableItemResponse> listItems(Optional<ConsumableCategoryCode> categoryCode) {
		List<ConsumableItem> items = categoryCode
				.map(code -> consumableItemRepository.findByActiveIsTrueAndCategoryCodeOrderBySortOrderAsc(code))
				.orElseGet(consumableItemRepository::findByActiveIsTrue);
		return items.stream()
				.sorted(Comparator
						.comparingInt((ConsumableItem c) -> c.getCategoryCode().getSortOrder())
						.thenComparingInt(ConsumableItem::getSortOrder))
				.map(ConsumableItemResponse::from)
				.toList();
	}
}
