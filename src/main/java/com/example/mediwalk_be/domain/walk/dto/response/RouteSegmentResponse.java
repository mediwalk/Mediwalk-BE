package com.example.mediwalk_be.domain.walk.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "경로 안내 타임라인 항목 (경로상 순서대로 ROUTE_GUIDE·PARK·MARKET + 목적지)")
public record RouteSegmentResponse(
		@Schema(description = "ROUTE_GUIDE | MARKET | PARK | DESTINATION")
		RouteSegmentType type,
		@Schema(description = "타임라인 순서 (1부터). DESTINATION은 null")
		Integer segmentIndex,
		@Schema(description = "MARKET·PARK·DESTINATION 장소명")
		String name,
		@Schema(description = "안내 문구. ROUTE_GUIDE는 구간당 짧은 한 문장, DESTINATION 포함, MARKET·PARK는 null")
		String instruction
) {
}
