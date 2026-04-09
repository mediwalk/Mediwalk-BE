package com.example.mediwalk_be.domain.reward.controller;

import com.example.mediwalk_be.domain.reward.dto.response.RewardMainResponse;
import com.example.mediwalk_be.domain.reward.service.RewardMainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reward-main")
@RequiredArgsConstructor
@Tag(name = "Reward", description = "리워드 도메인: 이벤트(수거·미션), 거래(적립·환급), 요약")
public class RewardMainController {

	private final RewardMainService rewardMainService;

	@GetMapping
	@Operation(summary = "리워드 메인 요약", description = "총 적립 리워드, 이번·지난 달 적립(해당 월 거래일 기준 ACCUMULATION 합계·모든 적립 유형 포함), 증가율, 누적 수거 횟수(totalCollectionsCount), 달성 목표(설명·카테고리 포함), 최근 적립·환급 내역을 반환합니다.")
	public ResponseEntity<RewardMainResponse> getRewardMain(
			@RequestParam Long userId,
			@RequestParam(defaultValue = "5") int achievementsLimit,
			@RequestParam(defaultValue = "10") int recentTransactionsSize) {
		return rewardMainService.getRewardMain(userId, achievementsLimit, recentTransactionsSize)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}
}
