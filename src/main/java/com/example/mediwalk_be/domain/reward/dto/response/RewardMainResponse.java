package com.example.mediwalk_be.domain.reward.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record RewardMainResponse(
	Long userId,
	Integer lastMonthRewardTotal,
	Integer thisMonthRewardTotal,
	/** 이번 달 폐의약품 수거 적립 합계 (원). 홈 화면 카드용 */
	Integer thisMonthMedicineCollectionRewardTotal,
	Integer totalAccumulatedReward,
	Double rewardIncreaseRateComparedToLastMonth,
	Integer totalCollectionsCount,
	Integer yearlyMedicineCollectionCount,
	List<RewardMainAchievementResponse> achievements,
	List<RewardMainTransactionResponse> recentRewardTransactions,
	LocalDateTime snapshotAt
) {
}
