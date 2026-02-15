package com.example.mediwalk_be.entity;

import com.example.mediwalk_be.entity.common.BaseEntity;
import com.example.mediwalk_be.entity.enums.ActivityLevel;
import com.example.mediwalk_be.entity.enums.SlopeLevel;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "route_filters")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RouteFilter extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "route_id", nullable = false, unique = true)
	private Route route;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ActivityLevel activityLevel;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private SlopeLevel slopeLevel;

	/** 휴식 포인트 배치 (공원 휴식 포인트 안내) */
	@Column(nullable = false)
	@Builder.Default
	private Boolean includeRestPoints = false;

	/** 자연 친화 (녹지율 높은 경로 우선) */
	@Column(nullable = false)
	@Builder.Default
	private Boolean natureFriendly = false;

	/** 보행자 전용 도로 우선 */
	@Column(nullable = false)
	@Builder.Default
	private Boolean pedestrianOnly = false;
}
