package com.example.mediwalk_be.domain.reward.service;

import com.example.mediwalk_be.domain.mission.entity.UserAchievement;
import com.example.mediwalk_be.domain.mission.service.UserAchievementService;
import com.example.mediwalk_be.domain.reward.dto.response.RewardMainAchievementResponse;
import com.example.mediwalk_be.domain.reward.dto.response.RewardMainResponse;
import com.example.mediwalk_be.domain.reward.dto.response.RewardMainTransactionResponse;
import com.example.mediwalk_be.domain.reward.entity.RewardTransaction;
import com.example.mediwalk_be.domain.reward.entity.enums.EventType;
import com.example.mediwalk_be.domain.reward.entity.enums.RewardTransactionType;
import com.example.mediwalk_be.domain.reward.repository.RewardTransactionRepository;
import com.example.mediwalk_be.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RewardMainService {

	private final UserService userService;
	private final UserAchievementService userAchievementService;
	private final RewardTransactionRepository rewardTransactionRepository;

	public Optional<RewardMainResponse> getRewardMain(Long userId, int achievementsLimit, int recentTransactionsSize) {
		var userOpt = userService.findById(userId);
		if (userOpt.isEmpty()) {
			return Optional.empty();
		}
		var user = userOpt.get();

		var rewardSummary = userService.getRewardMainMonthlySummary(userId);

		LocalDateTime now = LocalDateTime.now();
		int totalCollectionsCount = user.getTotalCollectionsCount();
		int yearlyMedicineCollectionCount = countYearlyMedicineCollections(userId);

		List<UserAchievement> userAchievements = userAchievementService.findByUserId(userId);
		userAchievements.sort(rewardMainAchievementSortComparator());
		if (achievementsLimit > 0 && userAchievements.size() > achievementsLimit) {
			userAchievements = userAchievements.subList(0, achievementsLimit);
		}
		List<RewardMainAchievementResponse> achievements = userAchievements.stream()
				.map(RewardMainAchievementResponse::from)
				.toList();

		int safeSize = recentTransactionsSize > 0 ? recentTransactionsSize : 10;
		Pageable pageable = PageRequest.of(0, safeSize);
		List<RewardTransaction> txs = rewardTransactionRepository.findByUserIdOrderByTransactionDateDesc(userId, pageable);
		List<RewardMainTransactionResponse> recentRewardTransactions = txs.stream()
				.map(RewardMainTransactionResponse::from)
				.toList();

		return Optional.of(new RewardMainResponse(
				userId,
				rewardSummary.lastMonthRewardTotal(),
				rewardSummary.thisMonthRewardTotal(),
				user.getTotalAccumulatedReward(),
				rewardSummary.rewardIncreaseRateComparedToLastMonth(),
				totalCollectionsCount,
				yearlyMedicineCollectionCount,
				achievements,
				recentRewardTransactions,
				now
		));
	}

	private int countYearlyMedicineCollections(Long userId) {
		ZoneId seoul = ZoneId.of("Asia/Seoul");
		LocalDate todaySeoul = ZonedDateTime.now(seoul).toLocalDate();
		LocalDateTime yearStart = todaySeoul.withDayOfYear(1).atStartOfDay();
		LocalDateTime yearEnd = ZonedDateTime.now(seoul).toLocalDateTime();
		Long count = rewardTransactionRepository.countAccumulatedEventsByUserIdAndMedicineCollectionBetween(
				userId,
				yearStart,
				yearEnd,
				RewardTransactionType.ACCUMULATION,
				EventType.MEDICINE_COLLECTION
		);
		return count != null ? Math.toIntExact(count) : 0;
	}

	private static Comparator<UserAchievement> rewardMainAchievementSortComparator() {
		return (a, b) -> {
			boolean aAchieved = Boolean.TRUE.equals(a.getIsAchieved());
			boolean bAchieved = Boolean.TRUE.equals(b.getIsAchieved());

			if (aAchieved != bAchieved) {
				return Boolean.compare(bAchieved, aAchieved);
			}

			if (aAchieved) {
				var aDate = a.getAchievedDate();
				var bDate = b.getAchievedDate();
				if (aDate == null && bDate == null) {
					return 0;
				}
				if (aDate == null) {
					return 1;
				}
				if (bDate == null) {
					return -1;
				}
				return bDate.compareTo(aDate);
			}

			int progressCmp = Integer.compare(b.getCurrentProgress(), a.getCurrentProgress());
			if (progressCmp != 0) {
				return progressCmp;
			}
			return b.getUpdatedAt().compareTo(a.getUpdatedAt());
		};
	}
}
