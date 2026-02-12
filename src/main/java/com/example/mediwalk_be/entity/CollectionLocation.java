package com.example.mediwalk_be.entity;

import com.example.mediwalk_be.entity.common.BaseEntity;
import com.example.mediwalk_be.entity.enums.CollectionLocationType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "collection_locations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CollectionLocation extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 장소명 (예: 강남구 보건소, 선릉역 약국) */
	@Column(nullable = false, length = 100)
	private String name;

	/** 도로명 주소 */
	@Column(nullable = false, length = 255)
	private String address;

	@Column(nullable = false)
	private Double latitude;

	@Column(nullable = false)
	private Double longitude;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private CollectionLocationType type;

	/** 기본 리워드 금액 (원). 명세: 3,000원 통일 */
	@Column(nullable = false)
	@Builder.Default
	private Integer baseRewardAmount = 3000;

	/** 인증 버튼 활성화 반경 (미터). 명세: 목적지 반경 20m 이내 접근 시 활성화 */
	@Column(nullable = false)
	@Builder.Default
	private Integer activationRadius = 20;
}
