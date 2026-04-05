package com.example.mediwalk_be.domain.walk.entity;

import com.example.mediwalk_be.domain.common.entity.BaseEntity;
import com.example.mediwalk_be.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "daily_steps",
		uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "date"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DailySteps extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	private LocalDate date;

	/** 해당 일 걸음 수 */
	@Column(nullable = false)
	@Builder.Default
	private Integer stepsCount = 0;

	/** 걸음 수 추가 */
	public void addSteps(int count) {
		this.stepsCount += count;
	}
}
