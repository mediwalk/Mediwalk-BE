package com.example.mediwalk_be.domain.reward.controller;

import com.example.mediwalk_be.domain.reward.dto.request.CreateRewardTransactionRequest;
import com.example.mediwalk_be.domain.reward.dto.response.RewardTransactionResponse;
import com.example.mediwalk_be.domain.reward.service.RewardTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reward-transactions")
@RequiredArgsConstructor
public class RewardTransactionController {

	private final RewardTransactionService rewardTransactionService;

	@PostMapping
	public ResponseEntity<RewardTransactionResponse> create(@RequestBody CreateRewardTransactionRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(RewardTransactionResponse.from(rewardTransactionService.create(request)));
	}

	@GetMapping("/{id}")
	public ResponseEntity<RewardTransactionResponse> findById(@PathVariable Long id) {
		return rewardTransactionService.findById(id)
				.map(RewardTransactionResponse::from)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping(params = "userId")
	public List<RewardTransactionResponse> findByUserId(
			@RequestParam Long userId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return rewardTransactionService.findByUserIdOrderByTransactionDateDesc(userId, PageRequest.of(page, size)).stream()
				.map(RewardTransactionResponse::from)
				.toList();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteById(@PathVariable Long id) {
		if (rewardTransactionService.findById(id).isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		rewardTransactionService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
