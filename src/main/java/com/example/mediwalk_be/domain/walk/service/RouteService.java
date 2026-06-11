package com.example.mediwalk_be.domain.walk.service;

import com.example.mediwalk_be.domain.walk.dto.request.CreateRouteRequest;
import com.example.mediwalk_be.domain.walk.dto.request.RouteFilterRequest;
import com.example.mediwalk_be.domain.walk.dto.request.RouteGenerationRequest;
import com.example.mediwalk_be.domain.walk.dto.response.GeneratedRouteResponse;
import com.example.mediwalk_be.domain.walk.dto.response.PointOfInterestResponse;
import com.example.mediwalk_be.domain.walk.dto.response.TmapRoutePreviewResponse;
import com.example.mediwalk_be.domain.walk.entity.CollectionLocation;
import com.example.mediwalk_be.domain.walk.entity.Route;
import com.example.mediwalk_be.domain.walk.entity.enums.ActivityLevel;
import com.example.mediwalk_be.domain.user.entity.User;
import com.example.mediwalk_be.domain.mission.entity.UserDailyMission;
import com.example.mediwalk_be.domain.walk.repository.CollectionLocationRepository;
import com.example.mediwalk_be.domain.walk.repository.PointOfInterestRepository;
import com.example.mediwalk_be.domain.walk.repository.RouteRepository;
import com.example.mediwalk_be.domain.walk.util.DistanceUtil;
import com.example.mediwalk_be.domain.mission.repository.UserDailyMissionRepository;
import com.example.mediwalk_be.domain.user.repository.UserRepository;
import com.example.mediwalk_be.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RouteService {

	private static final double ESTIMATE_METERS_PER_STEP = 0.75;
	private static final double TMAP_STRAIGHT_TO_WALK_RATIO = 1.3;
	private static final int MAXIMUM_MAX_TMAP_DISTANCE_METERS = 3000;
	private static final int MAX_TMAP_CANDIDATES_TO_TRY = 20;

	private final RouteRepository routeRepository;
	private final UserRepository userRepository;
	private final UserDailyMissionRepository userDailyMissionRepository;
	private final CollectionLocationRepository collectionLocationRepository;
	private final PointOfInterestRepository pointOfInterestRepository;
	private final TmapPedestrianRouteService tmapPedestrianRouteService;

	public Optional<Route> findById(Long id) {
		return routeRepository.findById(id);
	}

	public Route getById(Long id) {
		return routeRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Route not found: id=" + id));
	}

	public List<Route> findByUserIdOrderByGeneratedAtDesc(Long userId, Pageable pageable) {
		return routeRepository.findByUserIdOrderByGeneratedAtDesc(userId, pageable);
	}

	@Transactional
	public Route save(Route route) {
		return routeRepository.save(route);
	}


	@Transactional
	public Route create(CreateRouteRequest request) {
		User user = userRepository.findById(request.userId())
				.orElseThrow(() -> new NotFoundException("User not found: id=" + request.userId()));
		CollectionLocation destination = collectionLocationRepository.findById(request.destinationId())
				.orElseThrow(() -> new NotFoundException("CollectionLocation not found: id=" + request.destinationId()));
		UserDailyMission userDailyMission = request.userDailyMissionId() != null
				? userDailyMissionRepository.findById(request.userDailyMissionId()).orElse(null)
				: null;

		Route route = Route.builder()
				.user(user)
				.userDailyMission(userDailyMission)
				.destination(destination)
				.startLatitude(request.startLatitude())
				.startLongitude(request.startLongitude())
				.totalDistanceMeters(request.totalDistanceMeters())
				.estimatedWalkTimeMinutes(request.estimatedWalkTimeMinutes())
				.estimatedSteps(request.estimatedSteps() != null ? request.estimatedSteps() : 0)
				.activityLevel(request.activityLevel())
				.routePolyline(request.routePolyline())
				.hasRestPoints(Boolean.TRUE.equals(request.hasRestPoints()))
				.notifyEcoMart(Boolean.TRUE.equals(request.notifyEcoMart()))
				.notifyWalkingProgress(Boolean.TRUE.equals(request.notifyWalkingProgress()))
				.generatedAt(LocalDateTime.now())
				.build();
		return routeRepository.save(route);
	}

	@Transactional
	public void deleteById(Long id) {
		routeRepository.deleteById(id);
	}

	@Transactional
	public GeneratedRouteResponse generateRoute(RouteGenerationRequest request) {
		if (request.destinationIds() == null || request.destinationIds().isEmpty()) {
			throw new IllegalArgumentException("후보 목적지(destinationIds)가 비어 있습니다.");
		}
		RouteFilterRequest f = request.filter();
		if (f == null) {
			throw new IllegalArgumentException("filter는 필수입니다.");
		}
		List<RankedCandidate> rankedCandidates = rankCollectionLocationCandidates(
				request.destinationIds(),
				request.currentLatitude(),
				request.currentLongitude(),
				f.activityLevel());

		CollectionLocation destination;
		TmapRoutePreviewResponse tmap;
		if (f.activityLevel() == ActivityLevel.MAXIMUM) {
			var picked = pickMaximumWithinTmapCap(
					rankedCandidates,
					request.currentLatitude(),
					request.currentLongitude());
			destination = picked.location();
			tmap = picked.tmap();
		} else {
			RankedCandidate best = rankedCandidates.get(0);
			destination = best.location();
			tmap = previewTmapRoute(
					request.currentLatitude(),
					request.currentLongitude(),
					destination);
		}

		CreateRouteRequest createRequest = new CreateRouteRequest(
				request.userId(),
				null,
				destination.getId(),
				request.currentLatitude(),
				request.currentLongitude(),
				tmap.totalDistanceMeters(),
				tmap.estimatedWalkTimeMinutes(),
				tmap.estimatedSteps(),
				f.activityLevel(),
				tmap.encodedPolyline(),
				f.includeRestPoints(),
				f.notifyEcoMart(),
				f.notifyWalkingProgress());
		Route route = create(createRequest);
		return new GeneratedRouteResponse(route, tmap.guideSteps() != null ? tmap.guideSteps() : List.of());
	}


	private PickedRoute pickMaximumWithinTmapCap(
			List<RankedCandidate> rankedCandidates,
			double currentLatitude,
			double currentLongitude) {
		for (RankedCandidate candidate : rankedCandidates.stream().limit(MAX_TMAP_CANDIDATES_TO_TRY).toList()) {
			TmapRoutePreviewResponse preview = previewTmapRoute(currentLatitude, currentLongitude, candidate.location());
			if (preview.totalDistanceMeters() <= MAXIMUM_MAX_TMAP_DISTANCE_METERS) {
				return new PickedRoute(candidate.location(), preview);
			}
		}
		throw new IllegalArgumentException(
				"3km 이내 보행 경로로 도달 가능한 수거함을 찾지 못했습니다. 다른 위치에서 다시 시도해 주세요.");
	}

	private TmapRoutePreviewResponse previewTmapRoute(
			double currentLatitude,
			double currentLongitude,
			CollectionLocation destination) {
		String endName = destination.getName() != null && !destination.getName().isBlank()
				? destination.getName()
				: "도착";
		return tmapPedestrianRouteService.preview(
				currentLatitude,
				currentLongitude,
				destination.getLatitude(),
				destination.getLongitude(),
				"출발",
				endName,
				"0");
	}


	private List<RankedCandidate> rankCollectionLocationCandidates(
			List<Long> destinationIds,
			double currentLatitude,
			double currentLongitude,
			ActivityLevel activityLevel) {
		int targetSteps = targetStepsForActivity(activityLevel);
		List<RankedCandidate> ranked = new ArrayList<>();

		for (Long id : destinationIds.stream().distinct().toList()) {
			if (id == null) {
				continue;
			}
			CollectionLocation loc = collectionLocationRepository.findById(id).orElse(null);
			if (loc == null) {
				continue;
			}
			double meters = DistanceUtil.calculateDistanceMeters(
					currentLatitude,
					currentLongitude,
					loc.getLatitude(),
					loc.getLongitude());
			int estSteps = meters > 0 ? (int) Math.round(meters / ESTIMATE_METERS_PER_STEP) : 0;
			int score = Math.abs(estSteps - targetSteps);
			ranked.add(new RankedCandidate(loc, score, meters));
		}
		if (ranked.isEmpty()) {
			throw new IllegalArgumentException("유효한 수거함 목적지가 없습니다. destinationIds를 확인하세요.");
		}
		ranked.sort(Comparator.comparingInt(RankedCandidate::score)
				.thenComparingDouble(RankedCandidate::straightMeters));
		return ranked;
	}

	private record RankedCandidate(CollectionLocation location, int score, double straightMeters) {
	}

	private record PickedRoute(CollectionLocation location, TmapRoutePreviewResponse tmap) {
	}

	private static int targetStepsForActivity(ActivityLevel level) {
		int targetWalkingMeters = switch (level) {
			case MODERATE -> 500;
			case ACTIVE -> 1200;
			case MAXIMUM -> 2500;
		};
		double straightMeters = targetWalkingMeters / TMAP_STRAIGHT_TO_WALK_RATIO;
		return (int) Math.round(straightMeters / ESTIMATE_METERS_PER_STEP);
	}

	public List<PointOfInterestResponse> getRestPointsByRouteId(Long routeId) {
		return pointOfInterestRepository.findByRouteIdOrderByOrderAsc(routeId).stream()
				.map(PointOfInterestResponse::from)
				.toList();
	}
}
