package com.example.mediwalk_be.domain.walk.service;

import com.example.mediwalk_be.domain.walk.client.dto.tmap.TmapRouteGuideStep;
import com.example.mediwalk_be.domain.walk.dto.response.AlongRoutePoiResponse;
import com.example.mediwalk_be.domain.walk.dto.response.RouteSegmentResponse;
import com.example.mediwalk_be.domain.walk.dto.response.RouteSegmentType;
import com.example.mediwalk_be.domain.walk.entity.Route;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RouteSegmentBuilderService {

	private static final int ROUTE_GUIDE_SEGMENT_COUNT = 3;
	private static final int SHORT_STEP_METERS = 50;
	private static final String DESTINATION_INSTRUCTION = "목적지에 도착했어요.";
	private static final Pattern METERS_PATTERN = Pattern.compile("(\\d+)\\s*m");
	private static final Pattern ALONG_PATTERN = Pattern.compile("(.+?)를 따라");
	private static final Pattern STATION_PATTERN = Pattern.compile("([가-힣0-9]+역(?:\\s*\\d+번출구)?)");

	private final int maxPoiDistanceToPolylineMeters;

	public RouteSegmentBuilderService(
			@Value("${app.tmap.poi-max-distance-to-polyline-meters:200}") int maxPoiDistanceToPolylineMeters) {
		this.maxPoiDistanceToPolylineMeters = Math.max(50, maxPoiDistanceToPolylineMeters);
	}

	public List<RouteSegmentResponse> build(
			Route route,
			List<TmapRouteGuideStep> guideSteps,
			List<AlongRoutePoiResponse> martSuggestions,
			List<AlongRoutePoiResponse> parkSuggestions) {
		int totalDistance = route.getTotalDistanceMeters() != null ? route.getTotalDistanceMeters() : 0;
		if (totalDistance <= 0) {
			return List.of(destinationSegment(route));
		}

		List<OrderedSegment> ordered = new ArrayList<>();
		for (int segmentIndex = 1; segmentIndex <= ROUTE_GUIDE_SEGMENT_COUNT; segmentIndex++) {
			int start = (segmentIndex - 1) * totalDistance / ROUTE_GUIDE_SEGMENT_COUNT;
			int end = segmentIndex < ROUTE_GUIDE_SEGMENT_COUNT
					? segmentIndex * totalDistance / ROUTE_GUIDE_SEGMENT_COUNT
					: totalDistance;
			int segmentLength = end - start;
			String instruction = shortInstructionForThird(
					guideSteps,
					start,
					end,
					segmentIndex == ROUTE_GUIDE_SEGMENT_COUNT,
					segmentIndex,
					segmentLength);
			ordered.add(new OrderedSegment(
					sortFraction(start, totalDistance),
					segmentIndex * 2,
					new RouteSegmentResponse(RouteSegmentType.ROUTE_GUIDE, segmentIndex, null, instruction)));
		}

		AlongRoutePoiResponse mart = firstNearPolyline(martSuggestions);
		if (mart != null) {
			ordered.add(new OrderedSegment(
					sortFraction(mart.approxAlongRouteMeters(), totalDistance),
					3,
					new RouteSegmentResponse(RouteSegmentType.MARKET, null, mart.name(), null)));
		}

		AlongRoutePoiResponse park = firstNearPolyline(parkSuggestions);
		if (park != null) {
			ordered.add(new OrderedSegment(
					sortFraction(park.approxAlongRouteMeters(), totalDistance),
					5,
					new RouteSegmentResponse(RouteSegmentType.PARK, null, park.name(), null)));
		}

		ordered.add(new OrderedSegment(1.0, 9, destinationSegment(route)));

		ordered.sort(Comparator.comparingDouble(OrderedSegment::sortFraction)
				.thenComparingInt(OrderedSegment::tieBreaker));

		return assignTimelineIndices(ordered.stream().map(OrderedSegment::segment).toList());
	}

	/** 경로상 위치로 정렬된 뒤, PARK·MARKET을 포함해 1부터 연속 segmentIndex를 부여한다. DESTINATION은 null 유지. */
	private static List<RouteSegmentResponse> assignTimelineIndices(List<RouteSegmentResponse> segments) {
		int index = 1;
		List<RouteSegmentResponse> result = new ArrayList<>(segments.size());
		for (RouteSegmentResponse segment : segments) {
			if (segment.type() == RouteSegmentType.DESTINATION) {
				result.add(segment);
				continue;
			}
			result.add(new RouteSegmentResponse(
					segment.type(),
					index++,
					segment.name(),
					segment.instruction()));
		}
		return result;
	}

	private static RouteSegmentResponse destinationSegment(Route route) {
		String name = route.getDestination() != null ? route.getDestination().getName() : "목적지";
		return new RouteSegmentResponse(RouteSegmentType.DESTINATION, null, name, DESTINATION_INSTRUCTION);
	}

	private AlongRoutePoiResponse firstNearPolyline(List<AlongRoutePoiResponse> suggestions) {
		if (suggestions == null || suggestions.isEmpty()) {
			return null;
		}
		return suggestions.stream()
				.filter(p -> p.distanceToPolylineMeters() != null
						&& p.distanceToPolylineMeters() <= maxPoiDistanceToPolylineMeters)
				.findFirst()
				.orElse(null);
	}

	private static double sortFraction(int alongMeters, int totalDistance) {
		if (totalDistance <= 0) {
			return 0;
		}
		return Math.min(1.0, Math.max(0.0, (double) alongMeters / totalDistance));
	}

	private static String shortInstructionForThird(
			List<TmapRouteGuideStep> guideSteps,
			int segmentStartMeters,
			int segmentEndMeters,
			boolean lastSegment,
			int segmentIndex,
			int segmentLengthMeters) {
		String raw = bestInstructionInRange(guideSteps, segmentStartMeters, segmentEndMeters, lastSegment);
		if (raw != null) {
			return summarizeInstruction(raw, segmentIndex, segmentLengthMeters);
		}
		if (segmentLengthMeters > 0) {
			return "약 " + segmentLengthMeters + "m 이동해주세요";
		}
		return "이어서 이동해주세요";
	}

	/** 구간 안에서 거리·도로명이 가장 의미 있는 step을 선택 */
	private static String bestInstructionInRange(
			List<TmapRouteGuideStep> guideSteps,
			int segmentStartMeters,
			int segmentEndMeters,
			boolean lastSegment) {
		if (guideSteps == null || guideSteps.isEmpty()) {
			return null;
		}
		return guideSteps.stream()
				.filter(step -> step.instruction() != null && !step.instruction().isBlank())
				.filter(step -> {
					int along = step.alongRouteMeters();
					return lastSegment
							? along >= segmentStartMeters && along <= segmentEndMeters
							: along >= segmentStartMeters && along < segmentEndMeters;
				})
				.max(Comparator
						.<TmapRouteGuideStep>comparingInt(step -> {
							Integer meters = extractMeters(step.instruction());
							return meters != null ? meters : 0;
						})
						.thenComparingInt(step -> instructionWeight(step.instruction())))
				.map(step -> step.instruction().trim())
				.orElse(null);
	}

	private static int instructionWeight(String instruction) {
		int weight = instruction.length();
		if (instruction.contains("역")) {
			weight += 200;
		}
		if (instruction.contains(",")) {
			weight += 50;
		}
		return weight;
	}

	static String summarizeInstruction(String raw, int segmentIndex, int segmentLengthMeters) {
		if (raw == null || raw.isBlank()) {
			return segmentLengthMeters > 0
					? "약 " + segmentLengthMeters + "m 이동해주세요"
					: "이어서 이동해주세요";
		}

		String text = raw.trim();
		boolean hasCrosswalk = text.contains("횡단보도");
		Integer stepMeters = extractMeters(text);
		String landmark = extractLandmark(text);
		boolean shortStep = stepMeters == null || stepMeters < SHORT_STEP_METERS;

		if (segmentIndex == ROUTE_GUIDE_SEGMENT_COUNT) {
			if (landmark != null) {
				return landmark + " 쪽으로 이어서 걸어주세요";
			}
			if (segmentLengthMeters > 0) {
				return "목적지까지 약 " + segmentLengthMeters + "m 이동해주세요";
			}
			return "이어서 걸어주세요";
		}

		if (hasCrosswalk && segmentLengthMeters > 0) {
			return "횡단보도를 건너 약 " + segmentLengthMeters + "m 이동해주세요";
		}

		if (landmark != null) {
			if (segmentIndex == 1 && shortStep && stepMeters != null) {
				return landmark + "까지 " + stepMeters + "m 이동해주세요";
			}
			int displayMeters = shortStep ? segmentLengthMeters : stepMeters;
			if (displayMeters > 0) {
				return landmark + " 방향으로 약 " + displayMeters + "m 이동해주세요";
			}
			return landmark + " 방향으로 이동해주세요";
		}

		if (shortStep && segmentLengthMeters > 0) {
			return "약 " + segmentLengthMeters + "m 이동해주세요";
		}
		if (stepMeters != null) {
			return "약 " + stepMeters + "m 이동해주세요";
		}
		return text.length() > 36 ? text.substring(0, 33) + "..." : text;
	}

	private static Integer extractMeters(String text) {
		Matcher matcher = METERS_PATTERN.matcher(text);
		if (matcher.find()) {
			return Integer.parseInt(matcher.group(1));
		}
		return null;
	}

	private static String extractLandmark(String text) {
		Matcher stationMatcher = STATION_PATTERN.matcher(text);
		if (stationMatcher.find()) {
			return stationMatcher.group(1).trim();
		}
		if (text.contains("횡단보도")) {
			return "횡단보도";
		}
		Matcher alongMatcher = ALONG_PATTERN.matcher(text);
		String alongTarget = null;
		while (alongMatcher.find()) {
			alongTarget = alongMatcher.group(1).trim();
		}
		if (alongTarget != null && !alongTarget.isBlank()) {
			if (alongTarget.contains(" 후 ")) {
				String[] parts = alongTarget.split(" 후 ");
				alongTarget = parts[parts.length - 1].trim();
			}
			return trimTurnPrefix(alongTarget);
		}
		int commaIdx = text.indexOf(',');
		if (commaIdx > 0) {
			return trimTurnPrefix(text.substring(0, commaIdx).trim());
		}
		int turnIdx = text.indexOf("에서 ");
		if (turnIdx > 0) {
			return trimTurnPrefix(text.substring(0, turnIdx).trim());
		}
		return null;
	}

	private static String trimTurnPrefix(String value) {
		if (value.startsWith("좌회전 후 ")) {
			return value.substring("좌회전 후 ".length()).trim();
		}
		if (value.startsWith("우회전 후 ")) {
			return value.substring("우회전 후 ".length()).trim();
		}
		return value;
	}

	private record OrderedSegment(double sortFraction, int tieBreaker, RouteSegmentResponse segment) {
	}

}
