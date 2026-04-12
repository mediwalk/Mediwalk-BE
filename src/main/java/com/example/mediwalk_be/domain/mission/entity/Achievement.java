package com.example.mediwalk_be.domain.mission.entity;

import com.example.mediwalk_be.domain.common.entity.BaseEntity;
import com.example.mediwalk_be.domain.mission.entity.enums.AchievementCategory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
		name = "achievements",
		uniqueConstraints = @UniqueConstraint(name = "uk_achievements_code", columnNames = "code"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Achievement extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 업무·클라이언트 식별용 불변 코드 (예: ENV_NOVICE). */
	@Column(nullable = false, length = 64)
	private String code;

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
