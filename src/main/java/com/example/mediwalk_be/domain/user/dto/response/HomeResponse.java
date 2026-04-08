package com.example.mediwalk_be.domain.user.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record HomeResponse(
	Long userId,
	Integer lastMonthRewardTotal,
	Integer thisMonthRewardTotal,
	Integer totalAccumulatedReward,
	Double rewardIncreaseRateComparedToLastMonth,
	Integer thisMonthCollectionsCount,
	List<HomeAchievementResponse> achievements,
	List<HomeRewardTransactionResponse> recentRewardTransactions,
	LocalDateTime snapshotAt
) {
}

