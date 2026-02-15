package com.example.mediwalk_be.entity;

import com.example.mediwalk_be.entity.common.BaseEntity;
import com.example.mediwalk_be.entity.enums.MissionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "missions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Mission extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private MissionType missionType;

	/** 미션 명 (고정). 예: 오늘의 폐의약품 수거 미션, 오늘의 운동 미션 */
	@Column(nullable = false, length = 100)
	private String title;

	/** 미션 내용 (유동적). 예: 옆 동네 보건소에 버리고 오기 */
	@Column(nullable = false, length = 255)
	private String description;

	/** 기본 리워드 금액 (원) */
	@Column(nullable = false)
	@Builder.Default
	private Integer baseRewardAmount = 3000;
}
