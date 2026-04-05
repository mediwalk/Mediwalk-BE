package com.example.mediwalk_be.domain.mission.entity;

import com.example.mediwalk_be.domain.common.entity.BaseEntity;
import com.example.mediwalk_be.domain.mission.entity.enums.MissionStatus;
import com.example.mediwalk_be.domain.user.entity.User;
import com.example.mediwalk_be.domain.walk.entity.CollectionLocation;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_daily_missions",
		uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "mission_id", "mission_date"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserDailyMission extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "mission_id", nullable = false)
	private Mission mission;

	/** 목적지 (폐의약품 수거 미션인 경우 수거함 위치) */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "collection_location_id")
	private CollectionLocation collectionLocation;

	@Column(nullable = false)
	private LocalDate missionDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	@Builder.Default
	private MissionStatus status = MissionStatus.PENDING;

	private LocalDateTime completedAt;

	/** 획득한 리워드 (원) */
	private Integer earnedReward;

	/** 미션 완료 처리 */
	public void complete(LocalDateTime completedAt, Integer earnedReward) {
		this.completedAt = completedAt;
		this.earnedReward = earnedReward;
		this.status = MissionStatus.COMPLETED;
	}
}
