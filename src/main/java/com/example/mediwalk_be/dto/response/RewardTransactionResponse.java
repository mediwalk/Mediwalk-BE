package com.example.mediwalk_be.dto.response;

import com.example.mediwalk_be.entity.RewardTransaction;
import com.example.mediwalk_be.entity.enums.RewardTransactionType;

import java.time.LocalDateTime;

public record RewardTransactionResponse(
	Long id,
	Long userId,
	Long eventId,
	Integer amount,
	RewardTransactionType transactionType,
	LocalDateTime transactionDate,
	String description,
	String bankName,
	String accountNumberMasked,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static RewardTransactionResponse from(RewardTransaction e) {
		return new RewardTransactionResponse(
			e.getId(),
			e.getUser().getId(),
			e.getEvent() != null ? e.getEvent().getId() : null,
			e.getAmount(),
			e.getTransactionType(),
			e.getTransactionDate(),
			e.getDescription(),
			e.getBankName(),
			e.getAccountNumberMasked(),
			e.getCreatedAt(),
			e.getUpdatedAt()
		);
	}
}
