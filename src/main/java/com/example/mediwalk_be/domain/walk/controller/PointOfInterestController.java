package com.example.mediwalk_be.domain.walk.controller;

import com.example.mediwalk_be.domain.walk.dto.response.PointOfInterestResponse;
import com.example.mediwalk_be.domain.walk.service.PointOfInterestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/points-of-interest")
@RequiredArgsConstructor
@Tag(name = "PointOfInterest", description = "경로 안내 포인트: 벤치·마트·공원·목적지 유형의 휴식 포인트 조회")
public class PointOfInterestController {

	private final PointOfInterestService pointOfInterestService;

	@GetMapping("/{id}")
	@Operation(summary = "POI 단건 조회")
	public ResponseEntity<PointOfInterestResponse> findById(@PathVariable Long id) {
		return pointOfInterestService.findById(id)
				.map(PointOfInterestResponse::from)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping(params = "routeId")
	@Operation(summary = "경로별 POI 목록 조회", description = "routeId에 해당하는 휴식 포인트를 순서대로 반환합니다.")
	public List<PointOfInterestResponse> findByRouteId(@RequestParam Long routeId) {
		return pointOfInterestService.findByRouteIdOrderByOrderAsc(routeId).stream()
				.map(PointOfInterestResponse::from)
				.toList();
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "POI 삭제")
	public ResponseEntity<Void> deleteById(@PathVariable Long id) {
		if (pointOfInterestService.findById(id).isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		pointOfInterestService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
