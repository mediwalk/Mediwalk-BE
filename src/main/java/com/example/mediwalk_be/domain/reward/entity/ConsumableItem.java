package com.example.mediwalk_be.domain.reward.entity;

import com.example.mediwalk_be.domain.common.entity.BaseEntity;
import com.example.mediwalk_be.domain.reward.entity.enums.ConsumableCategoryCode;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "consumable_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ConsumableItem extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private ConsumableCategoryCode categoryCode;

	@Column(nullable = false, length = 120)
	private String name;

	@Column(nullable = false)
	private Integer priceWon;

	@Column(nullable = false)
	private int sortOrder;

	@Column(length = 512)
	private String imageUrl;

	@Column(nullable = false)
	@Builder.Default
	private boolean active = true;
}
