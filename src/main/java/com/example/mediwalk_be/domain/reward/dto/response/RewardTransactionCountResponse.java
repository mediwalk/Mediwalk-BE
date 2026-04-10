package com.example.mediwalk_be.domain.reward.dto.response;

public record RewardTransactionCountResponse(
		Long userId,
		Long totalCount
) {
}
