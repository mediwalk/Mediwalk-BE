package com.example.mediwalk_be.domain.user.service;

import com.example.mediwalk_be.domain.user.entity.User;
import com.example.mediwalk_be.domain.reward.entity.enums.EventType;
import com.example.mediwalk_be.domain.reward.entity.enums.RewardTransactionType;
import com.example.mediwalk_be.domain.reward.repository.RewardTransactionRepository;
import com.example.mediwalk_be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;
	private final RewardTransactionRepository rewardTransactionRepository;

	public Optional<User> findById(Long id) {
		return userRepository.findById(id);
	}

	public User getById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("User not found: id=" + id));
	}

	public Optional<User> findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	public Optional<User> findByFirebaseUid(String firebaseUid) {
		return userRepository.findByFirebaseUid(firebaseUid);
	}

	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	public List<User> findAll() {
		return userRepository.findAll();
	}

	@Transactional
	public User save(User user) {
		return userRepository.save(user);
	}

	@Transactional
	public void deleteById(Long id) {
		userRepository.deleteById(id);
	}

	// 리워드 메인용: 지난 달·이번 달 전체 적립 합계, 폐의약품 수거 적립 합계, 지난 달 대비 증가율
	public RewardMainMonthlySummary getRewardMainMonthlySummary(Long userId) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime thisMonthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
		LocalDateTime lastMonthStart = thisMonthStart.minusMonths(1);
		LocalDateTime lastMonthEnd = thisMonthStart.minusNanos(1);

		int thisMonthTotal = sumAccumulationInPeriod(userId, thisMonthStart, now);
		int lastMonthTotal = sumAccumulationInPeriod(userId, lastMonthStart, lastMonthEnd);
		int thisMonthMedicineCollectionTotal = sumMedicineCollectionInPeriod(userId, thisMonthStart, now);
		int lastMonthMedicineCollectionTotal = sumMedicineCollectionInPeriod(userId, lastMonthStart, lastMonthEnd);

		// 전체 적립 기준 증가율 (리워드 메인페이지용)
		Double increaseRate = null;
		if (lastMonthTotal > 0) {
			increaseRate = ((double) (thisMonthTotal - lastMonthTotal) / lastMonthTotal) * 100;
		} else if (thisMonthTotal > 0) {
			increaseRate = 100.0;
		}

		// 폐의약품 수거 적립 기준 증가율 (홈 화면 카드용)
		Double medicineCollectionIncreaseRate = null;
		if (lastMonthMedicineCollectionTotal > 0) {
			medicineCollectionIncreaseRate = ((double) (thisMonthMedicineCollectionTotal - lastMonthMedicineCollectionTotal) / lastMonthMedicineCollectionTotal) * 100;
		} else if (thisMonthMedicineCollectionTotal > 0) {
			medicineCollectionIncreaseRate = 100.0;
		}

		return new RewardMainMonthlySummary(lastMonthTotal, thisMonthTotal, thisMonthMedicineCollectionTotal, increaseRate, medicineCollectionIncreaseRate);
	}

	private int sumAccumulationInPeriod(Long userId, LocalDateTime start, LocalDateTime end) {
		Long sum = rewardTransactionRepository.sumAccumulatedAmountByUserIdAndTransactionTypeBetween(
				userId,
				start,
				end,
				RewardTransactionType.ACCUMULATION
		);
		return sum != null ? Math.toIntExact(sum) : 0;
	}

	private int sumMedicineCollectionInPeriod(Long userId, LocalDateTime start, LocalDateTime end) {
		Long sum = rewardTransactionRepository.sumAccumulatedAmountByUserIdAndMedicineCollectionBetween(
				userId,
				start,
				end,
				RewardTransactionType.ACCUMULATION,
				EventType.MEDICINE_COLLECTION
		);
		return sum != null ? Math.toIntExact(sum) : 0;
	}

	public record RewardMainMonthlySummary(
			int lastMonthRewardTotal,
			int thisMonthRewardTotal,
			int thisMonthMedicineCollectionRewardTotal,
			Double rewardIncreaseRateComparedToLastMonth,
			Double medicineCollectionRewardIncreaseRateComparedToLastMonth
	) {}
}
