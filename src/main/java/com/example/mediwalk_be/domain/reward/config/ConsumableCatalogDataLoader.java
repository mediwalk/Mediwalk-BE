package com.example.mediwalk_be.domain.reward.config;

import com.example.mediwalk_be.domain.reward.entity.ConsumableItem;
import com.example.mediwalk_be.domain.reward.entity.enums.ConsumableCategoryCode;
import com.example.mediwalk_be.domain.reward.repository.ConsumableItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 화면설계서 Detail Description에 명시된 소모품 마스터 최초 1회 적재.
 */
@Component
@RequiredArgsConstructor
public class ConsumableCatalogDataLoader implements ApplicationRunner {

	private final ConsumableItemRepository consumableItemRepository;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (consumableItemRepository.count() > 0) {
			return;
		}
		int o = 0;
		consumableItemRepository.save(ConsumableItem.builder()
				.categoryCode(ConsumableCategoryCode.BLOOD_GLUCOSE)
				.name("시험지(100매)")
				.priceWon(10_000)
				.sortOrder(o++)
				.build());
		consumableItemRepository.save(ConsumableItem.builder()
				.categoryCode(ConsumableCategoryCode.BLOOD_GLUCOSE)
				.name("일회용 채혈침(100개)")
				.priceWon(5_500)
				.sortOrder(o++)
				.build());
		consumableItemRepository.save(ConsumableItem.builder()
				.categoryCode(ConsumableCategoryCode.INSULIN)
				.name("인슐린 펜니들(100개)")
				.priceWon(16_000)
				.sortOrder(0)
				.build());
		consumableItemRepository.save(ConsumableItem.builder()
				.categoryCode(ConsumableCategoryCode.INSULIN)
				.name("인슐린 펌프 주입 세트")
				.priceWon(18_000)
				.sortOrder(1)
				.build());
		consumableItemRepository.save(ConsumableItem.builder()
				.categoryCode(ConsumableCategoryCode.CONTINUOUS_GLUCOSE)
				.name("CGM 센서")
				.priceWon(80_000)
				.sortOrder(0)
				.build());
		consumableItemRepository.save(ConsumableItem.builder()
				.categoryCode(ConsumableCategoryCode.HEALTH_FUNCTION)
				.name("당뇨환자용 영양조제식(캔/팩)")
				.priceWon(3_500)
				.sortOrder(0)
				.build());
		consumableItemRepository.save(ConsumableItem.builder()
				.categoryCode(ConsumableCategoryCode.HYPOGLYCEMIA_PREP)
				.name("휴대용 포도당 사탕(1통)")
				.priceWon(4_000)
				.sortOrder(0)
				.build());
		consumableItemRepository.save(ConsumableItem.builder()
				.categoryCode(ConsumableCategoryCode.HYPOGLYCEMIA_PREP)
				.name("농축 포도당 젤")
				.priceWon(2_000)
				.sortOrder(1)
				.build());
	}
}
