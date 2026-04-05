package com.example.mediwalk_be.dto.request;

import com.example.mediwalk_be.entity.enums.RewardTransactionType;

import java.time.LocalDateTime;

public record CreateRewardTransactionRequest(
	Long userId,
	Integer amount,
	RewardTransactionType transactionType,
	LocalDateTime transactionDate,
	String description,
	String bankName,
	String accountNumberMasked
) {
}
