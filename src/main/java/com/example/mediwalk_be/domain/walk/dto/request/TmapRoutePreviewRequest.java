package com.example.mediwalk_be.domain.walk.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Tmap 보행자 경로 미리보기 (Swagger 테스트용)")
public record TmapRoutePreviewRequest(
		@NotNull
		@Schema(description = "출발 위도 (WGS84)", example = "37.4979")
		Double startLatitude,
		@NotNull
		@Schema(description = "출발 경도 (WGS84)", example = "127.0276")
		Double startLongitude,
		@NotNull
		@Schema(description = "도착 위도 (WGS84)", example = "37.5665")
		Double endLatitude,
		@NotNull
		@Schema(description = "도착 경도 (WGS84)", example = "126.9780")
		Double endLongitude,
		@Schema(description = "출발지 표시 이름", example = "출발")
		String startName,
		@Schema(description = "도착지 표시 이름", example = "도착")
		String endName,
		@Schema(description = "Tmap searchOption: 0 추천, 10 최단, 30 최단+계단제외", example = "0")
		String searchOption
) {
}
