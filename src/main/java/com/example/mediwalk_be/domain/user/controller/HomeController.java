package com.example.mediwalk_be.domain.user.controller;

import com.example.mediwalk_be.domain.user.dto.response.HomeResponse;
import com.example.mediwalk_be.domain.user.service.HomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
@Tag(name = "Home", description = "홈·리워드 메인 요약(이번 달 적립, 누적 수거, 달성 목표, 최근 적립 내역)")
public class HomeController {

	private final HomeService homeService;

	@GetMapping
	@Operation(summary = "홈(리워드 메인) 요약", description = "총 적립 리워드, 이번·지난 달 적립, 증가율, 누적 수거 횟수(totalCollectionsCount), 달성 목표(설명·카테고리 포함), 최근 적립·환급 내역을 반환합니다.")
	public ResponseEntity<HomeResponse> getHome(
			@RequestParam Long userId,
			@RequestParam(defaultValue = "5") int achievementsLimit,
			@RequestParam(defaultValue = "10") int recentTransactionsSize) {
		return homeService.getHome(userId, achievementsLimit, recentTransactionsSize)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}
}

