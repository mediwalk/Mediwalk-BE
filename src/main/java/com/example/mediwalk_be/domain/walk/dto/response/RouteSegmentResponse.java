package com.example.mediwalk_be.domain.walk.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "경로 안내 타임라인 항목 (구간 요약 3개 + 마트·공원 + 목적지)")
public record RouteSegmentResponse(
		@Schema(description = "ROUTE_GUIDE | MARKET | PARK | DESTINATION")
		RouteSegmentType type,
		@Schema(description = "ROUTE_GUIDE일 때 1~3")
		Integer segmentIndex,
		@Schema(description = "MARKET·PARK·DESTINATION 장소명")
		String name,
		@Schema(description = "안내 문구. ROUTE_GUIDE는 구간당 짧은 한 문장, DESTINATION 포함, MARKET·PARK는 null")
		String instruction
) {
}
