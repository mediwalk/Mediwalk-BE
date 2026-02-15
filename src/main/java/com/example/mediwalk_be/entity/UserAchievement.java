package com.example.mediwalk_be.entity;

import com.example.mediwalk_be.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_achievements",
		uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "achievement_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserAchievement extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "achievement_id", nullable = false)
	private Achievement achievement;

	/** 현재 진행도 (회/보/원 등) */
	@Column(nullable = false)
	@Builder.Default
	private Integer currentProgress = 0;

	@Column(nullable = false)
	@Builder.Default
	private Boolean isAchieved = false;

	private LocalDateTime achievedDate;

	/** 진행도 추가. 목표 도달 시 달성 처리 */
	public void addProgress(int delta, int targetValue) {
		this.currentProgress += delta;
		if (this.currentProgress >= targetValue) {
			this.isAchieved = true;
			this.achievedDate = LocalDateTime.now();
		}
	}
}
