package com.example.mediwalk_be.domain.walk.controller;

import com.example.mediwalk_be.domain.walk.dto.request.TmapRoutePreviewRequest;
import com.example.mediwalk_be.domain.walk.dto.response.TmapRoutePreviewResponse;
import com.example.mediwalk_be.domain.walk.service.TmapPedestrianRouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/routes/tmap")
@RequiredArgsConstructor
@Tag(name = "Tmap", description = "Tmap 보행자 경로 (미리보기·테스트)")
public class TmapRoutePreviewController {

	private final TmapPedestrianRouteService tmapPedestrianRouteService;

	@PostMapping("/preview")
	@Operation(summary = "Tmap 보행 경로 미리보기", description = "폴리라인(인코딩), 총 거리·시간, 추정 걸음 수를 반환합니다. app.tmap.api-key 필요.")
	public ResponseEntity<TmapRoutePreviewResponse> preview(@Valid @RequestBody TmapRoutePreviewRequest request) {
		TmapRoutePreviewResponse body = tmapPedestrianRouteService.preview(
				request.startLatitude(),
				request.startLongitude(),
				request.endLatitude(),
				request.endLongitude(),
				request.startName(),
				request.endName(),
				request.searchOption());
		return ResponseEntity.ok(body);
	}
}
