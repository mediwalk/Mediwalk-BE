package com.example.mediwalk_be.domain.walk.dto.response;

import com.example.mediwalk_be.domain.walk.client.dto.tmap.TmapRouteGuideStep;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Tmap 보행 경로 미리보기 결과")
public record TmapRoutePreviewResponse(
		@Schema(description = "총 거리 (m)")
		int totalDistanceMeters,
		@Schema(description = "총 소요 시간 (초)")
		int totalTimeSeconds,
		@Schema(description = "예상 도보 시간 (분), totalTime 기준 올림")
		int estimatedWalkTimeMinutes,
		@Schema(description = "추정 걸음 수 (거리 ÷ 0.75m)")
		int estimatedSteps,
		@Schema(description = "Google Polyline Encoding 형식 (lat,lng 순서)")
		String encodedPolyline,
		@Schema(description = "경로 꼭짓점 수 (디버그)")
		int coordinateCount,
		@Schema(description = "Tmap 경로 안내 단계 (polyline 진행 거리 포함)")
		List<TmapRouteGuideStep> guideSteps
) {
}
