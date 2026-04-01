package com.example.mediwalk_be.service;

import com.example.mediwalk_be.dto.response.HomeAchievementResponse;
import com.example.mediwalk_be.dto.response.HomeRewardTransactionResponse;
import com.example.mediwalk_be.dto.response.HomeResponse;
import com.example.mediwalk_be.entity.RewardTransaction;
import com.example.mediwalk_be.entity.UserAchievement;
import com.example.mediwalk_be.entity.enums.EventType;
import com.example.mediwalk_be.entity.enums.RewardTransactionType;
import com.example.mediwalk_be.repository.RewardTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

	private final UserService userService;
	private final UserAchievementService userAchievementService;
	private final RewardTransactionRepository rewardTransactionRepository;

	public Optional<HomeResponse> getHome(Long userId, int achievementsLimit, int recentTransactionsSize) {
		if (userService.findById(userId).isEmpty()) {
			return Optional.empty();
		}

		// 상단 리워드 카드(지난달/이번달 합계, 증가율)
		var rewardSummary = userService.getRewardSummaryForHome(userId);

		// 이번달 "수거 수"(= MEDICINE_COLLECTION 이벤트로 발생한 적립 ACCUMULATION 건수)
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime thisMonthStart = now
				.withDayOfMonth(1)
				.withHour(0)
				.withMinute(0)
				.withSecond(0)
				.withNano(0);

		Long collectionsCount = rewardTransactionRepository.countAccumulatedEventsByUserIdAndMedicineCollectionBetween(
				userId,
				thisMonthStart,
				now,
				RewardTransactionType.ACCUMULATION,
				EventType.MEDICINE_COLLECTION
		);
		int thisMonthCollectionsCount = collectionsCount != null ? collectionsCount.intValue() : 0;

		// 달성 목표 리스트
		List<UserAchievement> userAchievements = userAchievementService.findByUserId(userId);
		userAchievements.sort(homeAchievementSortComparator());
		if (achievementsLimit > 0 && userAchievements.size() > achievementsLimit) {
			userAchievements = userAchievements.subList(0, achievementsLimit);
		}
		List<HomeAchievementResponse> achievements = userAchievements.stream()
				.map(HomeAchievementResponse::from)
				.toList();

		// 최근 적립/환급 내역
		int safeSize = recentTransactionsSize > 0 ? recentTransactionsSize : 10;
		Pageable pageable = PageRequest.of(0, safeSize);
		List<RewardTransaction> txs = rewardTransactionRepository.findByUserIdOrderByTransactionDateDesc(userId, pageable);
		List<HomeRewardTransactionResponse> recentRewardTransactions = txs.stream()
				.map(HomeRewardTransactionResponse::from)
				.toList();

		return Optional.of(new HomeResponse(
				userId,
				rewardSummary.lastMonthRewardTotal(),
				rewardSummary.thisMonthRewardTotal(),
				rewardSummary.rewardIncreaseRateComparedToLastMonth(),
				thisMonthCollectionsCount,
				achievements,
				recentRewardTransactions,
				now
		));
	}

	private static Comparator<UserAchievement> homeAchievementSortComparator() {
		return (a, b) -> {
			boolean aAchieved = Boolean.TRUE.equals(a.getIsAchieved());
			boolean bAchieved = Boolean.TRUE.equals(b.getIsAchieved());

			// 미달성 먼저
			if (aAchieved != bAchieved) {
				return Boolean.compare(aAchieved, bAchieved);
			}

			// 둘 다 미달성: 진행도 큰 순
			if (!aAchieved) {
				int progressCmp = Integer.compare(b.getCurrentProgress(), a.getCurrentProgress());
				if (progressCmp != 0) {
					return progressCmp;
				}
				// 동일 진행도: 최근 업데이트/생성 순
				return b.getUpdatedAt().compareTo(a.getUpdatedAt());
			}

			// 둘 다 달성: 달성일 최신 순(없으면 뒤)
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
		};
	}
}

