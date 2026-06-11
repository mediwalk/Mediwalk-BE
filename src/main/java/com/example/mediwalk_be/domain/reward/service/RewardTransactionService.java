package com.example.mediwalk_be.domain.reward.service;

import com.example.mediwalk_be.domain.reward.dto.request.CreateRewardTransactionRequest;
import com.example.mediwalk_be.domain.reward.entity.RewardTransaction;
import com.example.mediwalk_be.domain.reward.entity.enums.EventType;
import com.example.mediwalk_be.domain.user.entity.User;
import com.example.mediwalk_be.domain.reward.entity.enums.RewardTransactionType;
import com.example.mediwalk_be.domain.mission.service.AchievementProgressService;
import com.example.mediwalk_be.domain.reward.repository.RewardTransactionRepository;
import com.example.mediwalk_be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RewardTransactionService {

	private final RewardTransactionRepository rewardTransactionRepository;
	private final UserRepository userRepository;
	private final AchievementProgressService achievementProgressService;

	public Optional<RewardTransaction> findById(Long id) {
		return rewardTransactionRepository.findById(id);
	}

	public Optional<RewardTransaction> findByIdWithEvent(Long id) {
		return rewardTransactionRepository.findByIdWithEvent(id);
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

	public List<RewardTransaction> findByUserIdWithOptionalPeriod(
			Long userId,
			LocalDateTime startDateTime,
			LocalDateTime endDateTime,
			String sort,
			Pageable pageable
	) {
		LocalDateTime start = startDateTime;
		LocalDateTime end = endDateTime;
		String normalizedSort = normalizeSort(sort);
		boolean hasStart = start != null;
		boolean hasEnd = end != null;
		if (hasStart != hasEnd) {
			throw new IllegalArgumentException("기간 필터는 startDateTime, endDateTime을 함께 전달해야 합니다.");
		}
		if (hasStart && start != null && end != null && start.isAfter(end)) {
			throw new IllegalArgumentException("startDateTime은 endDateTime보다 이후일 수 없습니다.");
		}
		if (hasStart) {
			if ("oldest".equals(normalizedSort)) {
				return rewardTransactionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateAsc(
						userId,
						start,
						end,
						pageable
				);
			}
			return rewardTransactionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
						userId,
						start,
						end,
						pageable
				);
		}
		if ("oldest".equals(normalizedSort)) {
			return rewardTransactionRepository.findByUserIdOrderByTransactionDateAsc(userId, pageable);
		}
		return rewardTransactionRepository.findByUserIdOrderByTransactionDateDesc(userId, pageable);
	}

	public List<RewardTransaction> findMedicineCollectionTransactions(
			Long userId,
			LocalDateTime startDateTime,
			LocalDateTime endDateTime,
			String sort,
			Pageable pageable
	) {
		LocalDateTime start = startDateTime;
		LocalDateTime end = endDateTime;
		String normalizedSort = normalizeSort(sort);
		boolean hasStart = start != null;
		boolean hasEnd = end != null;
		if (hasStart != hasEnd) {
			throw new IllegalArgumentException("기간 필터는 startDateTime, endDateTime을 함께 전달해야 합니다.");
		}
		if (hasStart && start != null && end != null && start.isAfter(end)) {
			throw new IllegalArgumentException("startDateTime은 endDateTime보다 이후일 수 없습니다.");
		}

		if (hasStart) {
			if ("oldest".equals(normalizedSort)) {
				return rewardTransactionRepository
						.findByUserIdAndTransactionTypeAndAmountGreaterThanAndEventEventTypeAndTransactionDateBetweenOrderByTransactionDateAsc(
								userId,
								RewardTransactionType.ACCUMULATION,
								0,
								EventType.MEDICINE_COLLECTION,
								start,
								end,
								pageable
						);
			}
			return rewardTransactionRepository
					.findByUserIdAndTransactionTypeAndAmountGreaterThanAndEventEventTypeAndTransactionDateBetweenOrderByTransactionDateDesc(
							userId,
							RewardTransactionType.ACCUMULATION,
							0,
							EventType.MEDICINE_COLLECTION,
							start,
							end,
							pageable
					);
		}

		if ("oldest".equals(normalizedSort)) {
			return rewardTransactionRepository
					.findByUserIdAndTransactionTypeAndAmountGreaterThanAndEventEventTypeOrderByTransactionDateAsc(
							userId,
							RewardTransactionType.ACCUMULATION,
							0,
							EventType.MEDICINE_COLLECTION,
							pageable
					);
		}
		return rewardTransactionRepository
				.findByUserIdAndTransactionTypeAndAmountGreaterThanAndEventEventTypeOrderByTransactionDateDesc(
						userId,
						RewardTransactionType.ACCUMULATION,
						0,
						EventType.MEDICINE_COLLECTION,
						pageable
				);
	}

	public long countByUserIdWithOptionalPeriod(
			Long userId,
			LocalDateTime startDateTime,
			LocalDateTime endDateTime
	) {
		LocalDateTime start = startDateTime;
		LocalDateTime end = endDateTime;
		boolean hasStart = start != null;
		boolean hasEnd = end != null;
		if (hasStart != hasEnd) {
			throw new IllegalArgumentException("기간 필터는 startDateTime, endDateTime을 함께 전달해야 합니다.");
		}
		if (hasStart && start != null && end != null && start.isAfter(end)) {
			throw new IllegalArgumentException("startDateTime은 endDateTime보다 이후일 수 없습니다.");
		}
		if (hasStart) {
			Long count = rewardTransactionRepository.countAccumulatedEventsByUserIdAndMedicineCollectionBetween(
					userId,
					start,
					end,
					RewardTransactionType.ACCUMULATION,
					EventType.MEDICINE_COLLECTION
			);
			return count != null ? count : 0L;
		}
		Long count = rewardTransactionRepository.countAccumulatedEventsByUserIdAndMedicineCollection(
				userId,
				RewardTransactionType.ACCUMULATION,
				EventType.MEDICINE_COLLECTION
		);
		return count != null ? count : 0L;
	}

	private String normalizeSort(String sort) {
		if (sort == null || sort.isBlank()) {
			return "latest";
		}
		String normalized = sort.toLowerCase(Locale.ROOT);
		if (!"latest".equals(normalized) && !"oldest".equals(normalized)) {
			throw new IllegalArgumentException("sort는 latest 또는 oldest만 허용됩니다.");
		}
		return normalized;
	}

	@Transactional
	public RewardTransaction save(RewardTransaction rewardTransaction) {
		return rewardTransactionRepository.save(rewardTransaction);
	}

	/** 환급 신청. 최소 10,000원, 사용자 잔액 검증. 적립(ACCUMULATION)은 이벤트 생성 시 EventService에서만 처리하므로 이 메서드는 REFUND만 허용한다. */
	@Transactional
	public RewardTransaction create(CreateRewardTransactionRequest request) {
		User user = userRepository.findById(request.userId())
				.orElseThrow(() -> new IllegalArgumentException("User not found: id=" + request.userId()));

		if (request.transactionType() != RewardTransactionType.REFUND) {
			throw new IllegalArgumentException("리워드 거래 생성은 환급(REFUND)만 지원합니다. 적립 내역은 이벤트 생성을 통해 자동으로 처리됩니다.");
		}
		if (request.bankName() == null || request.bankName().isBlank()) {
			throw new IllegalArgumentException("환급 시 은행명은 필수입니다.");
		}
		if (request.accountNumberMasked() == null || request.accountNumberMasked().isBlank()) {
			throw new IllegalArgumentException("환급 시 계좌번호는 필수입니다.");
		}

		int refundAmount = request.amount() != null ? request.amount() : 0;
		if (refundAmount > 0) {
			refundAmount = -refundAmount;
		}
		if (refundAmount > -10_000) {
			throw new IllegalArgumentException("환급 최소 금액은 10,000원입니다.");
		}
		int currentTotalReward = user.getTotalAccumulatedReward() != null ? user.getTotalAccumulatedReward() : 0;
		if (currentTotalReward + refundAmount < 0) {
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
		RewardTransaction saved = rewardTransactionRepository.save(tx);
		achievementProgressService.syncRewardAmountAchievements(user);
		return saved;
	}

	@Transactional
	public void deleteById(Long id) {
		rewardTransactionRepository.deleteById(id);
	}
}
