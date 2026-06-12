package com.example.mediwalk_be.domain.reward.dto.request;

import com.example.mediwalk_be.domain.reward.entity.enums.RewardTransactionType;

import java.time.LocalDateTime;

public record CreateRewardTransactionRequest(
	Integer amount,
	RewardTransactionType transactionType,
	LocalDateTime transactionDate,
	String description,
	String bankName,
	String accountNumberMasked
) {
}
