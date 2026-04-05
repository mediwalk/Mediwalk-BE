package com.example.mediwalk_be.domain.walk.controller;

import com.example.mediwalk_be.domain.walk.dto.response.RouteFilterResponse;
import com.example.mediwalk_be.domain.walk.service.RouteFilterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/route-filters")
@RequiredArgsConstructor
public class RouteFilterController {

	private final RouteFilterService routeFilterService;

	@GetMapping
	public List<RouteFilterResponse> findAll() {
		return routeFilterService.findAll().stream()
				.map(RouteFilterResponse::from)
				.toList();
	}

	@GetMapping("/{id}")
	public ResponseEntity<RouteFilterResponse> findById(@PathVariable Long id) {
		return routeFilterService.findById(id)
				.map(RouteFilterResponse::from)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteById(@PathVariable Long id) {
		if (routeFilterService.findById(id).isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		routeFilterService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
