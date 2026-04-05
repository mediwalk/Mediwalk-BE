package com.example.mediwalk_be.domain.walk.controller;

import com.example.mediwalk_be.domain.walk.dto.request.CreateRouteRequest;
import com.example.mediwalk_be.domain.walk.dto.request.RouteGenerationRequest;
import com.example.mediwalk_be.domain.walk.dto.response.RouteResponse;
import com.example.mediwalk_be.domain.walk.entity.Route;
import com.example.mediwalk_be.domain.walk.service.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

	private final RouteService routeService;

	@PostMapping
	public ResponseEntity<RouteResponse> create(@RequestBody CreateRouteRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(RouteResponse.from(routeService.create(request)));
	}

	@PostMapping("/generate")
	public ResponseEntity<RouteResponse> generateRoute(@Valid @RequestBody RouteGenerationRequest request) {
		Route route = routeService.generateRoute(request);
		// 휴식 포인트(POI) 조회
		var restPoints = routeService.getRestPointsByRouteId(route.getId());
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(RouteResponse.from(route, restPoints));
	}

	@GetMapping("/{id}")
	public ResponseEntity<RouteResponse> findById(@PathVariable Long id) {
		return routeService.findById(id)
				.map(route -> {
					var restPoints = routeService.getRestPointsByRouteId(route.getId());
					return RouteResponse.from(route, restPoints);
				})
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping(params = "userId")
	public List<RouteResponse> findByUserId(
			@RequestParam Long userId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return routeService.findByUserIdOrderByGeneratedAtDesc(userId, PageRequest.of(page, size)).stream()
				.map(RouteResponse::from)
				.toList();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteById(@PathVariable Long id) {
		if (routeService.findById(id).isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		routeService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
