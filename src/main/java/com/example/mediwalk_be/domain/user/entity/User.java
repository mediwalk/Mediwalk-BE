package com.example.mediwalk_be.domain.user.entity;

import com.example.mediwalk_be.domain.common.entity.BaseEntity;
import com.example.mediwalk_be.domain.user.entity.enums.Gender;
import com.example.mediwalk_be.domain.user.entity.enums.UserRole;
import com.example.mediwalk_be.domain.user.entity.enums.UserStatus;
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

	/** Firebase Auth UID (Google 로그인). 기존 이메일만 가입한 계정은 null */
	@Column(unique = true, length = 128)
	private String firebaseUid;

	/** Google 등 OAuth 프로필 이미지 URL (마이페이지). 없으면 클라이언트 기본 이미지 */
	@Column(length = 1024)
	private String profileImageUrl;

	/** 리워드 적립/환급 반영 (양수: 적립, 음수: 환급) */
	public void addAccumulatedReward(int amount) {
		this.totalAccumulatedReward = Math.max(0, this.totalAccumulatedReward + amount);
	}

	/** 누적 수거 횟수 1 증가 (폐의약품 수거 인증 완료 시) */
	public void incrementTotalCollectionsCount() {
		this.totalCollectionsCount++;
	}

	/** Google 계정 연동 시 사용 */
	public void assignFirebaseUid(String uid) {
		if (uid == null || uid.isBlank()) {
			return;
		}
		if (this.firebaseUid != null && !this.firebaseUid.equals(uid)) {
			throw new IllegalStateException("Firebase uid already set for user id=" + this.id);
		}
		this.firebaseUid = uid;
	}

	public void updateProfileImageUrl(String url) {
		if (url == null || url.isBlank()) {
			return;
		}
		this.profileImageUrl = url.trim();
	}
}
