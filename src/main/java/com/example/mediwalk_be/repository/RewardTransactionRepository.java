package com.example.mediwalk_be.repository;

import com.example.mediwalk_be.entity.RewardTransaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RewardTransactionRepository extends JpaRepository<RewardTransaction, Long> {

	List<RewardTransaction> findByUserIdOrderByTransactionDateDesc(Long userId, Pageable pageable);

	List<RewardTransaction> findByUserIdAndTransactionDateBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
