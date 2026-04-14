package com.example.mediwalk_be.domain.mission.entity.enums;

import lombok.Getter;

@Getter
public enum AchievementCode {

	ENV_NOVICE(
			"ENV_NOVICE",
			"초보 환경 지킴이",
			"폐의약품 수거 인증 10회 이상 완료",
			AchievementCategory.ENVIRONMENTAL_PROTECTOR,
			10,
			"회",
			"environment"
	),
	ENV_RELIABLE(
			"ENV_RELIABLE",
			"의젓한 환경 지킴이",
			"폐의약품 수거 인증 50회 이상 완료",
			AchievementCategory.ENVIRONMENTAL_PROTECTOR,
			50,
			"회",
			"environment"
	),
	ENV_GUARDIAN(
			"ENV_GUARDIAN",
			"지구 환경 수호자",
			"폐의약품 수거 인증 100회 이상 누적 완료",
			AchievementCategory.ENVIRONMENTAL_PROTECTOR,
			100,
			"회",
			"environment"
	),
	WALK_NEWBIE(
			"WALK_NEWBIE",
			"새내기 걸음마",
			"누적 걸음 5만 보 이상",
			AchievementCategory.WALKING_EXPERT,
			50_000,
			"보",
			"walking"
	),
	WALK_HEALTHY(
			"WALK_HEALTHY",
			"건강한 걷기 전문가",
			"한 달 누적 걸음 10만 보 이상",
			AchievementCategory.WALKING_EXPERT,
			100_000,
			"보",
			"walking"
	),
	WALK_IRON(
			"WALK_IRON",
			"철인 메디 워커",
			"연속 3개월 한 달 15만 보 이상 (달성 판정은 별도 규칙 연동 예정)",
			AchievementCategory.WALKING_EXPERT,
			150_000,
			"보",
			"walking"
	),
	SAVE_THRIFTY(
			"SAVE_THRIFTY",
			"알뜰한 수집가",
			"누적 리워드 1만 원 이상",
			AchievementCategory.SAVINGS_MASTER,
			10_000,
			"원",
			"savings"
	),
	SAVE_EXPERT(
			"SAVE_EXPERT",
			"절약의 달인",
			"누적 리워드 5만 원 이상",
			AchievementCategory.SAVINGS_MASTER,
			50_000,
			"원",
			"savings"
	),
	SAVE_ASSET(
			"SAVE_ASSET",
			"리워드 자산",
			"누적 리워드 10만 원 이상",
			AchievementCategory.SAVINGS_MASTER,
			100_000,
			"원",
			"savings"
	);

	private final String dbCode;
	private final String displayName;
	private final String description;
	private final AchievementCategory category;
	private final int targetValue;
	private final String unit;
	private final String iconType;

	AchievementCode(
			String dbCode,
			String displayName,
			String description,
			AchievementCategory category,
			int targetValue,
			String unit,
			String iconType) {
		this.dbCode = dbCode;
		this.displayName = displayName;
		this.description = description;
		this.category = category;
		this.targetValue = targetValue;
		this.unit = unit;
		this.iconType = iconType;
	}
}
