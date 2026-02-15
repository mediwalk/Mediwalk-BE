package com.example.mediwalk_be.entity;

import com.example.mediwalk_be.entity.common.BaseEntity;
import com.example.mediwalk_be.entity.enums.ActivityLevel;
import com.example.mediwalk_be.entity.enums.SlopeLevel;
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
	private SlopeLevel averageSlope;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private ActivityLevel activityLevel;

	/** 경로 폴리라인 (Google Maps 등) */
	@Column(columnDefinition = "TEXT")
	private String routePolyline;

	/** 녹지율 (%) - 환경 맞춤형 필터 */
	private Double greenSpaceRatio;

	/** 횡단보도 개수 */
	private Integer crosswalkCount;

	/** 보행자 전용 경로 우선 여부 */
	@Column(nullable = false)
	@Builder.Default
	private Boolean isPedestrianOnly = false;

	/** 자연 친화 경로 여부 */
	@Column(nullable = false)
	@Builder.Default
	private Boolean isNatureFriendly = false;

	/** 휴식 포인트 포함 여부 */
	@Column(nullable = false)
	@Builder.Default
	private Boolean hasRestPoints = false;

	@Column(nullable = false)
	private LocalDateTime generatedAt;

	private LocalDateTime completedAt;
}
