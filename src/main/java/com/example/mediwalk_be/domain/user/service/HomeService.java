package com.example.mediwalk_be.domain.user.service;

import com.example.mediwalk_be.domain.user.dto.response.HomeAchievementResponse;
import com.example.mediwalk_be.domain.user.dto.response.HomeRewardTransactionResponse;
import com.example.mediwalk_be.domain.user.dto.response.HomeResponse;
import com.example.mediwalk_be.domain.reward.entity.RewardTransaction;
import com.example.mediwalk_be.domain.mission.entity.UserAchievement;
import com.example.mediwalk_be.domain.mission.service.UserAchievementService;
import com.example.mediwalk_be.domain.reward.repository.RewardTransactionRepository;
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
		var userOpt = userService.findById(userId);
		if (userOpt.isEmpty()) {
			return Optional.empty();
		}
		var user = userOpt.get();

		// 상단 리워드 카드(지난달/이번달 합계, 증가율)
		var rewardSummary = userService.getRewardSummaryForHome(userId);

		// 누적 수거 수 (사용자 누적 집계값 사용)
		LocalDateTime now = LocalDateTime.now();
		int totalCollectionsCount = user.getTotalCollectionsCount();

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
				user.getTotalAccumulatedReward(),
				rewardSummary.rewardIncreaseRateComparedToLastMonth(),
				totalCollectionsCount,
				achievements,
				recentRewardTransactions,
				now
		));
	}

	private static Comparator<UserAchievement> homeAchievementSortComparator() {
		return (a, b) -> {
			boolean aAchieved = Boolean.TRUE.equals(a.getIsAchieved());
			boolean bAchieved = Boolean.TRUE.equals(b.getIsAchieved());

			// 달성 먼저
			if (aAchieved != bAchieved) {
				return Boolean.compare(bAchieved, aAchieved);
			}

			// 둘 다 달성: 달성일 최신 순(없으면 뒤)
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

			// 둘 다 미달성: 진행도 큰 순
			int progressCmp = Integer.compare(b.getCurrentProgress(), a.getCurrentProgress());
			if (progressCmp != 0) {
				return progressCmp;
			}
			// 동일 진행도: 최근 업데이트/생성 순
			return b.getUpdatedAt().compareTo(a.getUpdatedAt());
		};
	}
}

