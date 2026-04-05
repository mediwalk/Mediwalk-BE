package com.example.mediwalk_be.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record HomeResponse(
	Long userId,
	Integer lastMonthRewardTotal,
	Integer thisMonthRewardTotal,
	Double rewardIncreaseRateComparedToLastMonth,
	Integer thisMonthCollectionsCount,
	List<HomeAchievementResponse> achievements,
	List<HomeRewardTransactionResponse> recentRewardTransactions,
	LocalDateTime snapshotAt
) {
}

