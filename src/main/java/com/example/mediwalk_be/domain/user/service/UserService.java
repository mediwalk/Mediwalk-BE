package com.example.mediwalk_be.domain.user.service;

import com.example.mediwalk_be.domain.user.entity.User;
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

	// 홈 화면용: 지난 달·이번 달 해당 기간에 발생한 모든 적립(ACCUMULATION) 합계 및 지난 달 대비 증가율
	public RewardSummaryForHome getRewardSummaryForHome(Long userId) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime thisMonthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
		LocalDateTime lastMonthStart = thisMonthStart.minusMonths(1);
		LocalDateTime lastMonthEnd = thisMonthStart.minusNanos(1);

		int thisMonthTotal = sumAccumulationInPeriod(userId, thisMonthStart, now);
		int lastMonthTotal = sumAccumulationInPeriod(userId, lastMonthStart, lastMonthEnd);

		Double increaseRate = null;
		if (lastMonthTotal > 0) {
			increaseRate = ((double) (thisMonthTotal - lastMonthTotal) / lastMonthTotal) * 100;
		} else if (thisMonthTotal > 0) {
			// 지난 달 0원, 이번 달 있을 경우 100% 증가로 간주
			increaseRate = 100.0;
		}

		return new RewardSummaryForHome(lastMonthTotal, thisMonthTotal, increaseRate);
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

	public record RewardSummaryForHome(int lastMonthRewardTotal, int thisMonthRewardTotal, Double rewardIncreaseRateComparedToLastMonth) {}
}
