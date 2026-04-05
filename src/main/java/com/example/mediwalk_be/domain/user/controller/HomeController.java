package com.example.mediwalk_be.domain.user.controller;

import com.example.mediwalk_be.domain.user.dto.response.HomeResponse;
import com.example.mediwalk_be.domain.user.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

	private final HomeService homeService;

	@GetMapping
	public ResponseEntity<HomeResponse> getHome(
			@RequestParam Long userId,
			@RequestParam(defaultValue = "5") int achievementsLimit,
			@RequestParam(defaultValue = "10") int recentTransactionsSize) {
		return homeService.getHome(userId, achievementsLimit, recentTransactionsSize)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}
}

