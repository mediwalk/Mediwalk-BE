package com.example.mediwalk_be.repository;

import com.example.mediwalk_be.entity.RewardTransaction;
import com.example.mediwalk_be.entity.enums.EventType;
import com.example.mediwalk_be.entity.enums.RewardTransactionType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RewardTransactionRepository extends JpaRepository<RewardTransaction, Long> {

	List<RewardTransaction> findByUserIdOrderByTransactionDateDesc(Long userId, Pageable pageable);

	List<RewardTransaction> findByUserIdAndTransactionDateBetween(Long userId, LocalDateTime start, LocalDateTime end);

	// 폐의약품 수거 이벤트로 발생한 적립(ACCUMULATION)만 합산. event 없는 적립은 제외
	@Query("""
			SELECT COALESCE(SUM(rt.amount), 0) FROM RewardTransaction rt
			JOIN rt.event e
			WHERE rt.user.id = :userId
			AND rt.transactionDate >= :start AND rt.transactionDate <= :end
			AND rt.transactionType = :transactionType
			AND rt.amount > 0
			AND e.eventType = :eventType
			""")
	Long sumAccumulatedAmountByUserIdAndMedicineCollectionBetween(
			@Param("userId") Long userId,
			@Param("start") LocalDateTime start,
			@Param("end") LocalDateTime end,
			@Param("transactionType") RewardTransactionType transactionType,
			@Param("eventType") EventType eventType);

	// 폐의약품 수거 이벤트로 발생한 적립(ACCUMULATION) 건수 집계. event 없는 적립은 제외
	@Query("""
			SELECT COALESCE(COUNT(rt.id), 0) FROM RewardTransaction rt
			JOIN rt.event e
			WHERE rt.user.id = :userId
			AND rt.transactionDate >= :start AND rt.transactionDate <= :end
			AND rt.transactionType = :transactionType
			AND rt.amount > 0
			AND e.eventType = :eventType
			""")
	Long countAccumulatedEventsByUserIdAndMedicineCollectionBetween(
			@Param("userId") Long userId,
			@Param("start") LocalDateTime start,
			@Param("end") LocalDateTime end,
			@Param("transactionType") RewardTransactionType transactionType,
			@Param("eventType") EventType eventType);
}
