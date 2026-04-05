package com.example.mediwalk_be.domain.user.dto.response;

import com.example.mediwalk_be.domain.reward.entity.RewardTransaction;
import com.example.mediwalk_be.domain.reward.entity.enums.RewardTransactionType;

import java.time.LocalDateTime;

public record HomeRewardTransactionResponse(
	Long id,
	Long userId,
	Long eventId,
	String eventTitle,
	String locationName,
	Integer amount,
	RewardTransactionType transactionType,
	LocalDateTime transactionDate,
	String description,
	String bankName,
	String accountNumberMasked,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static HomeRewardTransactionResponse from(RewardTransaction e) {
		return new HomeRewardTransactionResponse(
				e.getId(),
				e.getUser().getId(),
				e.getEvent() != null ? e.getEvent().getId() : null,
				e.getEvent() != null ? e.getEvent().getTitle() : null,
				e.getEvent() != null ? e.getEvent().getLocationName() : null,
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

