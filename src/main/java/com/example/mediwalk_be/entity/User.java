package com.example.mediwalk_be.entity;

import com.example.mediwalk_be.entity.common.BaseEntity;
import com.example.mediwalk_be.entity.enums.Gender;
import com.example.mediwalk_be.entity.enums.UserRole;
import com.example.mediwalk_be.entity.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 255)
	private String email;

	@Column(nullable = false, length = 255)
	private String password;

	@Column(nullable = false, length = 50)
	private String name;

	@Column(length = 20)
	private String phone;

	private LocalDate birthDate;

	@Enumerated(EnumType.STRING)
	@Column(length = 10)
	private Gender gender;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	@Builder.Default
	private UserRole role = UserRole.USER;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	@Builder.Default
	private UserStatus status = UserStatus.ACTIVE;

	/** 총 누적 리워드 (원 단위) */
	@Column(nullable = false)
	@Builder.Default
	private Integer totalAccumulatedReward = 0;

	/** 누적 수거 횟수 */
	@Column(nullable = false)
	@Builder.Default
	private Integer totalCollectionsCount = 0;

	/** 현재 위치 위도 (경로/거리 계산용) */
	private Double currentLatitude;

	/** 현재 위치 경도 */
	private Double currentLongitude;

	/** 리워드 적립/환급 반영 (양수: 적립, 음수: 환급) */
	public void addAccumulatedReward(int amount) {
		this.totalAccumulatedReward = Math.max(0, this.totalAccumulatedReward + amount);
	}

	/** 누적 수거 횟수 1 증가 (폐의약품 수거 인증 완료 시) */
	public void incrementTotalCollectionsCount() {
		this.totalCollectionsCount++;
	}
}
