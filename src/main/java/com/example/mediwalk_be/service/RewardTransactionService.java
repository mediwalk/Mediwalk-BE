package com.example.mediwalk_be.service;

import com.example.mediwalk_be.dto.request.CreateRewardTransactionRequest;
import com.example.mediwalk_be.entity.RewardTransaction;
import com.example.mediwalk_be.entity.User;
import com.example.mediwalk_be.entity.enums.RewardTransactionType;
import com.example.mediwalk_be.repository.RewardTransactionRepository;
import com.example.mediwalk_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RewardTransactionService {

	private final RewardTransactionRepository rewardTransactionRepository;
	private final UserRepository userRepository;

	public Optional<RewardTransaction> findById(Long id) {
		return rewardTransactionRepository.findById(id);
	}

	public RewardTransaction getById(Long id) {
		return rewardTransactionRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("RewardTransaction not found: id=" + id));
	}

	public List<RewardTransaction> findByUserIdOrderByTransactionDateDesc(Long userId, Pageable pageable) {
		return rewardTransactionRepository.findByUserIdOrderByTransactionDateDesc(userId, pageable);
	}

	public List<RewardTransaction> findByUserIdAndTransactionDateBetween(Long userId, LocalDateTime start, LocalDateTime end) {
		return rewardTransactionRepository.findByUserIdAndTransactionDateBetween(userId, start, end);
	}

	@Transactional
	public RewardTransaction save(RewardTransaction rewardTransaction) {
		return rewardTransactionRepository.save(rewardTransaction);
	}

	/** 환급 신청. 최소 10,000원, 사용자 잔액 검증 */
	@Transactional
	public RewardTransaction create(CreateRewardTransactionRequest request) {
		User user = userRepository.findById(request.userId())
				.orElseThrow(() -> new IllegalArgumentException("User not found: id=" + request.userId()));

		if (request.transactionType() == RewardTransactionType.REFUND) {
			int refundAmount = request.amount() != null ? request.amount() : 0;
			if (refundAmount > 0) {
				refundAmount = -refundAmount;
			}
			if (refundAmount > -10_000) {
				throw new IllegalArgumentException("환급 최소 금액은 10,000원입니다.");
			}
			if (user.getTotalAccumulatedReward() + refundAmount < 0) {
				throw new IllegalArgumentException("잔액이 부족합니다.");
			}
			user.addAccumulatedReward(refundAmount);
			LocalDateTime transactionDate = request.transactionDate() != null ? request.transactionDate() : LocalDateTime.now();
			RewardTransaction tx = RewardTransaction.builder()
					.user(user)
					.event(null)
					.amount(refundAmount)
					.transactionType(RewardTransactionType.REFUND)
					.transactionDate(transactionDate)
					.description(request.description() != null ? request.description() : "리워드 환급")
					.bankName(request.bankName())
					.accountNumberMasked(request.accountNumberMasked())
					.build();
			return rewardTransactionRepository.save(tx);
		}

		// ACCUMULATION은 Event 생성 시 EventService에서 처리
		int amount = request.amount() != null ? request.amount() : 0;
		LocalDateTime transactionDate = request.transactionDate() != null ? request.transactionDate() : LocalDateTime.now();
		RewardTransaction tx = RewardTransaction.builder()
				.user(user)
				.event(null)
				.amount(amount)
				.transactionType(request.transactionType())
				.transactionDate(transactionDate)
				.description(request.description())
				.bankName(request.bankName())
				.accountNumberMasked(request.accountNumberMasked())
				.build();
		return rewardTransactionRepository.save(tx);
	}

	@Transactional
	public void deleteById(Long id) {
		rewardTransactionRepository.deleteById(id);
	}
}
