package com.example.mediwalk_be.domain.user.dto.response;

import com.example.mediwalk_be.domain.reward.entity.Event;
import com.example.mediwalk_be.domain.reward.entity.RewardTransaction;
import com.example.mediwalk_be.domain.reward.entity.enums.EventType;
import com.example.mediwalk_be.domain.reward.entity.enums.RewardTransactionType;

import java.time.LocalDateTime;

public record HomeRewardTransactionResponse(
	Long id,
	Long userId,
	Long eventId,
	String eventTitle,
	String locationName,
	EventType eventType,
	Boolean accumulationCompleted,
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
		Event event = e.getEvent();
		return new HomeRewardTransactionResponse(
				e.getId(),
				e.getUser().getId(),
				event != null ? event.getId() : null,
				event != null ? event.getTitle() : null,
				event != null ? event.getLocationName() : null,
				event != null ? event.getEventType() : null,
				isAccumulationCompleted(e),
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

	private static boolean isAccumulationCompleted(RewardTransaction e) {
		if (e.getTransactionType() != RewardTransactionType.ACCUMULATION
				|| e.getAmount() == null
				|| e.getAmount() <= 0) {
			return false;
		}
		if (e.getEvent() == null) {
			return true;
		}
		return e.getEvent().getEventType() == EventType.MEDICINE_COLLECTION;
	}
}

