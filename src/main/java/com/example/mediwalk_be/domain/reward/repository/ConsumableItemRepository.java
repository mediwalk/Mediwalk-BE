package com.example.mediwalk_be.domain.reward.repository;

import com.example.mediwalk_be.domain.reward.entity.ConsumableItem;
import com.example.mediwalk_be.domain.reward.entity.enums.ConsumableCategoryCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsumableItemRepository extends JpaRepository<ConsumableItem, Long> {

	long countByCategoryCodeAndActiveIsTrue(ConsumableCategoryCode categoryCode);

	List<ConsumableItem> findByActiveIsTrueAndCategoryCodeOrderBySortOrderAsc(ConsumableCategoryCode categoryCode);

	List<ConsumableItem> findByActiveIsTrue();
}
