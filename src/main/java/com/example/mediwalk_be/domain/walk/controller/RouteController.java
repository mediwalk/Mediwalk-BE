package com.example.mediwalk_be.domain.walk.controller;

import com.example.mediwalk_be.domain.walk.dto.request.CreateRouteRequest;
import com.example.mediwalk_be.domain.walk.dto.request.RouteGenerationRequest;
import com.example.mediwalk_be.domain.walk.client.dto.tmap.TmapRouteGuideStep;
import com.example.mediwalk_be.domain.walk.dto.response.AlongRoutePoiResponse;
import com.example.mediwalk_be.domain.walk.dto.response.GeneratedRouteResponse;
import com.example.mediwalk_be.domain.walk.dto.response.RouteResponse;
import com.example.mediwalk_be.domain.walk.entity.Route;
import com.example.mediwalk_be.config.security.AuthenticatedUser;
import com.example.mediwalk_be.domain.walk.service.RouteAlongPoiSuggestionService;
import com.example.mediwalk_be.domain.walk.service.RouteSegmentBuilderService;
import com.example.mediwalk_be.domain.walk.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
@Tag(name = "Route", description = "운동 경로: 맞춤 경로 생성 및 조회")
public class RouteController {

	private final RouteService routeService;
	private final RouteAlongPoiSuggestionService routeAlongPoiSuggestionService;
	private final RouteSegmentBuilderService routeSegmentBuilderService;

	@PostMapping
	@Operation(summary = "경로 직접 생성", description = "Tmap 없이 경로 데이터를 직접 저장합니다. (관리·테스트용 — 앱은 POST /routes/generate 사용)")
	public ResponseEntity<RouteResponse> create(@RequestBody CreateRouteRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(RouteResponse.from(routeService.create(request)));
	}

	@PostMapping("/generate")
	@Operation(summary = "맞춤 경로 생성",
			description = "destinationIds 후보 중 filter.activityLevel에 맞는 수거함을 고른 뒤 Tmap 보행 경로를 산출합니다.")
	public ResponseEntity<RouteResponse> generateRoute(
			@AuthenticationPrincipal AuthenticatedUser currentUser,
			@Valid @RequestBody RouteGenerationRequest request) {
		GeneratedRouteResponse generated = routeService.generateRoute(currentUser.userId(), request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(toRouteResponseWithAlongPois(generated.route(), generated.guideSteps()));
	}

	@GetMapping("/{id}")
	@Operation(summary = "경로 단건 조회")
	public ResponseEntity<RouteResponse> findById(@PathVariable Long id) {
		return routeService.findById(id)
				.map(route -> toRouteResponseWithAlongPois(route, List.of()))
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping
	@Operation(summary = "사용자 경로 목록 조회", description = "생성일 내림차순으로 페이징 반환합니다.")
	public List<RouteResponse> findByUserId(
			@AuthenticationPrincipal AuthenticatedUser currentUser,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return routeService.findByUserIdOrderByGeneratedAtDesc(currentUser.userId(), PageRequest.of(page, size)).stream()
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

	private RouteResponse toRouteResponseWithAlongPois(Route route, List<TmapRouteGuideStep> guideSteps) {
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
		var routeSegments = routeSegmentBuilderService.build(route, guideSteps, marts, parks);

		return RouteResponse.from(route, restPoints, marts, parks, routeSegments);
	}
}
