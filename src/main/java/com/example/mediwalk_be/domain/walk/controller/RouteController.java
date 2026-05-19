package com.example.mediwalk_be.domain.walk.controller;

import com.example.mediwalk_be.domain.walk.dto.request.CreateRouteRequest;
import com.example.mediwalk_be.domain.walk.dto.request.RouteGenerationRequest;
import com.example.mediwalk_be.domain.walk.dto.response.AlongRoutePoiResponse;
import com.example.mediwalk_be.domain.walk.dto.response.RouteResponse;
import com.example.mediwalk_be.domain.walk.entity.Route;
import com.example.mediwalk_be.domain.walk.service.RouteAlongPoiSuggestionService;
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
	private final RouteAlongPoiSuggestionService routeAlongPoiSuggestionService;

	@PostMapping
	@Operation(summary = "경로 직접 생성")
	public ResponseEntity<RouteResponse> create(@RequestBody CreateRouteRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(RouteResponse.from(routeService.create(request)));
	}

	@PostMapping("/generate")
	@Operation(summary = "맞춤 경로 생성",
			description = "destinationIds 후보 중 filter.activityLevel에 맞는 수거함을 고른 뒤 Tmap 보행 경로를 산출합니다. "
					+ "notifyEcoMart·hasRestPoints가 참이면 경로 폴리라인을 따라 Tmap 주변 POI로 마트·공원 후보 목록을 같이 채웁니다(키·카테고리 설정 필요).")
	public ResponseEntity<RouteResponse> generateRoute(@Valid @RequestBody RouteGenerationRequest request) {
		Route route = routeService.generateRoute(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(toRouteResponseWithAlongPois(route));
	}

	@GetMapping("/{id}")
	@Operation(summary = "경로 단건 조회")
	public ResponseEntity<RouteResponse> findById(@PathVariable Long id) {
		return routeService.findById(id)
				.map(this::toRouteResponseWithAlongPois)
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

	private RouteResponse toRouteResponseWithAlongPois(Route route) {
		var restPoints = routeService.getRestPointsByRouteId(route.getId());
		String poly = route.getRoutePolyline();

		List<AlongRoutePoiResponse> marts =
				Boolean.TRUE.equals(route.getNotifyEcoMart()) && poly != null && !poly.isBlank()
						? routeAlongPoiSuggestionService.collectMartSuggestions(poly)
						: List.of();
		List<AlongRoutePoiResponse> parks =
				Boolean.TRUE.equals(route.getHasRestPoints()) && poly != null && !poly.isBlank()
						? routeAlongPoiSuggestionService.collectParkSuggestions(poly)
						: List.of();

		return RouteResponse.from(route, restPoints, marts, parks);
	}
}
