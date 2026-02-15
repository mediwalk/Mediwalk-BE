package com.example.mediwalk_be.entity;

import com.example.mediwalk_be.entity.common.BaseEntity;
import com.example.mediwalk_be.entity.enums.PointOfInterestType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "points_of_interest")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PointOfInterest extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "route_id", nullable = false)
	private Route route;

	/** 장소명 (예: 용산공원 입구 벤치) */
	@Column(nullable = false, length = 100)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private PointOfInterestType type;

	@Column(nullable = false)
	private Double latitude;

	@Column(nullable = false)
	private Double longitude;

	/** 경로상 순서 (MySQL 예약어 order 회피) */
	@Column(nullable = false, name = "display_order")
	private Integer order;

	/** 이전 지점으로부터의 거리 (미터). 예: 300m 앞 공원 벤치 */
	private Integer distanceFromPrevious;

	/** 안내 멘트. 예: 300m 앞 공원 벤치에서 잠시 쉬어가세요 */
	@Column(length = 500)
	private String instruction;
}
