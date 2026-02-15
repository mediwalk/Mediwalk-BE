package com.example.mediwalk_be.service;

import com.example.mediwalk_be.entity.RewardTransaction;
import com.example.mediwalk_be.repository.RewardTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RewardTransactionService {

	private final RewardTransactionRepository rewardTransactionRepository;

	public Optional<RewardTransaction> findById(Long id) {
		return rewardTransactionRepository.findById(id);
	}

	public RewardTransaction getById(Long id) {
		return rewardTransactionRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("RewardTransaction not found: id=" + id));
	}

	public List<RewardTransaction> findByUserIdOrderByTransactionDateDesc(Long userId, Pageable pageable) {
		return rewardTransactionRepository.findByUserIdOrderByTransactionDateDesc(userId, pageable);
	}

	public List<RewardTransaction> findByUserIdAndTransactionDateBetween(Long userId, LocalDateTime start, LocalDateTime end) {
		return rewardTransactionRepository.findByUserIdAndTransactionDateBetween(userId, start, end);
	}

	@Transactional
	public RewardTransaction save(RewardTransaction rewardTransaction) {
		return rewardTransactionRepository.save(rewardTransaction);
	}

	@Transactional
	public void deleteById(Long id) {
		rewardTransactionRepository.deleteById(id);
	}
}
