package com.example.mediwalk_be.entity;

import com.example.mediwalk_be.entity.common.BaseEntity;
import com.example.mediwalk_be.entity.enums.RewardTransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reward_transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RewardTransaction extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "event_id")
	private Event event;

	/** 거래 금액 (양수: 적립, 음수: 환급) */
	@Column(nullable = false)
	private Integer amount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private RewardTransactionType transactionType;

	@Column(nullable = false)
	private LocalDateTime transactionDate;

	/** 표기용 설명 */
	@Column(length = 100)
	private String description;

	/** 환급 시: 은행명 */
	@Column(length = 50)
	private String bankName;

	/** 환급 시: 마스킹된 계좌번호 (예: ****1234) */
	@Column(length = 20)
	private String accountNumberMasked;
}
