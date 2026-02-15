package com.example.mediwalk_be.entity;

import com.example.mediwalk_be.entity.common.BaseEntity;
import com.example.mediwalk_be.entity.enums.EventType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Event extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private EventType eventType;

	/** 이벤트 제목 (고정). 예: 폐의약품 수거, 운동 미션 완료 */
	@Column(nullable = false, length = 100)
	private String title;

	/** 리워드 금액 (원). 0원 가능 */
	@Column(nullable = false)
	@Builder.Default
	private Integer rewardAmount = 0;

	@Column(nullable = false)
	private LocalDateTime eventDateTime;

	/** 목적지/장소명 (표기용). 예: 강남구보건소 */
	@Column(length = 100)
	private String locationName;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "collection_location_id")
	private CollectionLocation collectionLocation;

	/** 인증 사진 URL (폐의약품 수거 인증 시) */
	@Column(length = 512)
	private String imageUrl;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "route_id")
	private Route route;
}
