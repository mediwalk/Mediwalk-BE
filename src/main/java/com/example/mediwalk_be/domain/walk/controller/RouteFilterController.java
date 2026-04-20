package com.example.mediwalk_be.domain.walk.controller;

import com.example.mediwalk_be.domain.walk.dto.response.RouteFilterResponse;
import com.example.mediwalk_be.domain.walk.service.RouteFilterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/route-filters")
@RequiredArgsConstructor
@Tag(name = "RouteFilter", description = "경로 필터 이력: 사용자가 선택한 AI 경로 필터 저장 이력 조회")
public class RouteFilterController {

	private final RouteFilterService routeFilterService;

	@GetMapping
	@Operation(summary = "경로 필터 전체 조회")
	public List<RouteFilterResponse> findAll() {
		return routeFilterService.findAll().stream()
				.map(RouteFilterResponse::from)
				.toList();
	}

	@GetMapping("/{id}")
	@Operation(summary = "경로 필터 단건 조회")
	public ResponseEntity<RouteFilterResponse> findById(@PathVariable Long id) {
		return routeFilterService.findById(id)
				.map(RouteFilterResponse::from)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "경로 필터 삭제")
	public ResponseEntity<Void> deleteById(@PathVariable Long id) {
		if (routeFilterService.findById(id).isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		routeFilterService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
