package com.example.mediwalk_be.domain.reward.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record RewardMainResponse(
	Long userId,
	Integer lastMonthRewardTotal,
	Integer thisMonthRewardTotal,
	Integer totalAccumulatedReward,
	Double rewardIncreaseRateComparedToLastMonth,
	Integer totalCollectionsCount,
	Integer yearlyMedicineCollectionCount,
	List<RewardMainAchievementResponse> achievements,
	List<RewardMainTransactionResponse> recentRewardTransactions,
	LocalDateTime snapshotAt
) {
}
