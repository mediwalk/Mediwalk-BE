package com.example.mediwalk_be.service;

import com.example.mediwalk_be.entity.RewardTransaction;
import com.example.mediwalk_be.entity.User;
import com.example.mediwalk_be.entity.enums.RewardTransactionType;
import com.example.mediwalk_be.repository.RewardTransactionRepository;
import com.example.mediwalk_be.repository.UserRepository;
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

	// 홈 화면용: 지난 달·이번 달 적립 리워드 합계 및 지난 달 대비 증가율 계산
	public RewardSummaryForHome getRewardSummaryForHome(Long userId) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime thisMonthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
		LocalDateTime lastMonthStart = thisMonthStart.minusMonths(1);

		List<RewardTransaction> thisMonthList = rewardTransactionRepository.findByUserIdAndTransactionDateBetween(userId, thisMonthStart, now);
		List<RewardTransaction> lastMonthList = rewardTransactionRepository.findByUserIdAndTransactionDateBetween(userId, lastMonthStart, thisMonthStart.minusNanos(1));

		int thisMonthTotal = thisMonthList.stream()
				.filter(t -> t.getTransactionType() == RewardTransactionType.ACCUMULATION && t.getAmount() != null && t.getAmount() > 0)
				.mapToInt(RewardTransaction::getAmount)
				.sum();
		int lastMonthTotal = lastMonthList.stream()
				.filter(t -> t.getTransactionType() == RewardTransactionType.ACCUMULATION && t.getAmount() != null && t.getAmount() > 0)
				.mapToInt(RewardTransaction::getAmount)
				.sum();

		Double increaseRate = null;
		if (lastMonthTotal > 0) {
			increaseRate = ((double) (thisMonthTotal - lastMonthTotal) / lastMonthTotal) * 100;
		} else if (thisMonthTotal > 0) {
			// 지난 달 0원, 이번 달 있을 경우 100% 증가로 간주
			increaseRate = 100.0;
		}

		return new RewardSummaryForHome(lastMonthTotal, thisMonthTotal, increaseRate);
	}

	public record RewardSummaryForHome(int lastMonthRewardTotal, int thisMonthRewardTotal, Double rewardIncreaseRateComparedToLastMonth) {}
}
