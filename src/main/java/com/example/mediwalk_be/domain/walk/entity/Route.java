package com.example.mediwalk_be.domain.walk.entity;

import com.example.mediwalk_be.domain.common.entity.BaseEntity;
import com.example.mediwalk_be.domain.mission.entity.UserDailyMission;
import com.example.mediwalk_be.domain.user.entity.User;
import com.example.mediwalk_be.domain.walk.entity.enums.ActivityLevel;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "routes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Route extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_daily_mission_id")
	private UserDailyMission userDailyMission;

	/** 시작 위치 */
	private Double startLatitude;
	private Double startLongitude;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "destination_id", nullable = false)
	private CollectionLocation destination;

	/** 총 거리 (미터) */
	@Column(nullable = false)
	private Integer totalDistanceMeters;

	/** 예상 도보 시간 (분) */
	@Column(nullable = false)
	private Integer estimatedWalkTimeMinutes;

	/** 예상 걸음 수 */
	private Integer estimatedSteps;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private ActivityLevel activityLevel;

	/** 경로 폴리라인 (Google Maps 등) */
	@Column(columnDefinition = "TEXT")
	private String routePolyline;

	/** 휴식 포인트 포함 여부 */
	@Column(nullable = false)
	@Builder.Default
	private Boolean hasRestPoints = false;

	/** 친환경 농산물 마트 안내 알림 */
	@Column(nullable = false)
	@Builder.Default
	private Boolean notifyEcoMart = false;

	/** 산책 진행률(목적지까지 거리 비율 등) 알림 */
	@Column(nullable = false)
	@Builder.Default
	private Boolean notifyWalkingProgress = false;

	@Column(nullable = false)
	private LocalDateTime generatedAt;

	private LocalDateTime completedAt;
}
