package com.example.mediwalk_be.domain.walk.controller;

import com.example.mediwalk_be.domain.walk.dto.request.CreateRouteRequest;
import com.example.mediwalk_be.domain.walk.dto.request.RouteGenerationRequest;
import com.example.mediwalk_be.domain.walk.dto.response.RouteResponse;
import com.example.mediwalk_be.domain.walk.entity.Route;
import com.example.mediwalk_be.domain.walk.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Route", description = "운동 경로: 맞춤 경로 생성 및 조회")
public class RouteController {

	private final RouteService routeService;

	@PostMapping
	@Operation(summary = "경로 직접 생성")
	public ResponseEntity<RouteResponse> create(@RequestBody CreateRouteRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(RouteResponse.from(routeService.create(request)));
	}

	@PostMapping("/generate")
	@Operation(summary = "맞춤 경로 생성", description = "destinationIds에 여러 수거함 id를 넣으면, filter.activityLevel 목표 걸음(약 2천/4천/6천 보)에 가장 가까운 후보를 고른 뒤 Tmap 보행 경로를 한 번 산출합니다. 저장된 Route의 destinationId가 선택된 목적지입니다. filter에 휴식 포인트·마트·진행 알림 옵션을 함께 전달할 수 있습니다.")
	public ResponseEntity<RouteResponse> generateRoute(@Valid @RequestBody RouteGenerationRequest request) {
		Route route = routeService.generateRoute(request);
		var restPoints = routeService.getRestPointsByRouteId(route.getId());
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(RouteResponse.from(route, restPoints));
	}

	@GetMapping("/{id}")
	@Operation(summary = "경로 단건 조회")
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
	@Operation(summary = "사용자 경로 목록 조회", description = "생성일 내림차순으로 페이징 반환합니다.")
	public List<RouteResponse> findByUserId(
			@RequestParam Long userId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return routeService.findByUserIdOrderByGeneratedAtDesc(userId, PageRequest.of(page, size)).stream()
				.map(RouteResponse::from)
				.toList();
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "경로 삭제")
	public ResponseEntity<Void> deleteById(@PathVariable Long id) {
		if (routeService.findById(id).isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		routeService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
