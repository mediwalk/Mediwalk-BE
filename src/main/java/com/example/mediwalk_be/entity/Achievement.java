package com.example.mediwalk_be.entity;

import com.example.mediwalk_be.entity.common.BaseEntity;
import com.example.mediwalk_be.entity.enums.AchievementCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "achievements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Achievement extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 목표명. 예: 초보 환경 지킴이, 건강한 걷기 전문가 */
	@Column(nullable = false, length = 100)
	private String name;

	/** 설명. 예: 폐의약품 수거 인증 10회 이상 완료 */
	@Column(nullable = false, length = 255)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private AchievementCategory category;

	/** 목표 값 (회, 보, 원 등) */
	@Column(nullable = false)
	private Integer targetValue;

	/** 단위. 예: 회, 보, 원 */
	@Column(nullable = false, length = 10)
	private String unit;

	/** UI 아이콘 유형 */
	@Column(length = 30)
	private String iconType;
}
