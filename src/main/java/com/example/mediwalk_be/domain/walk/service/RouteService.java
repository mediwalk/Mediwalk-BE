package com.example.mediwalk_be.domain.walk.service;

import com.example.mediwalk_be.domain.walk.dto.request.CreateRouteRequest;
import com.example.mediwalk_be.domain.walk.dto.request.RouteFilterRequest;
import com.example.mediwalk_be.domain.walk.dto.request.RouteGenerationRequest;
import com.example.mediwalk_be.domain.walk.dto.response.PointOfInterestResponse;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RouteService {

	/** {@link TmapPedestrianRouteService} 예상 걸음 산출과 동일한 보수적 가정(미터/보) */
	private static final double ESTIMATE_METERS_PER_STEP = 0.75;

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
				.orElseThrow(() -> new IllegalArgumentException("Route not found: id=" + id));
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
				.orElseThrow(() -> new IllegalArgumentException("User not found: id=" + request.userId()));
		CollectionLocation destination = collectionLocationRepository.findById(request.destinationId())
				.orElseThrow(() -> new IllegalArgumentException("CollectionLocation not found: id=" + request.destinationId()));
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
	public Route generateRoute(RouteGenerationRequest request) {
		if (request.destinationIds() == null || request.destinationIds().isEmpty()) {
			throw new IllegalArgumentException("후보 목적지(destinationIds)가 비어 있습니다.");
		}
		RouteFilterRequest f = request.filter();
		if (f == null) {
			throw new IllegalArgumentException("filter는 필수입니다.");
		}
		CollectionLocation destination = pickOptimalCollectionLocation(
				request.destinationIds(),
				request.currentLatitude(),
				request.currentLongitude(),
				f.activityLevel());

		String searchOption = "0";
		String endName = destination.getName() != null && !destination.getName().isBlank()
				? destination.getName()
				: "도착";
		var tmap = tmapPedestrianRouteService.preview(
				request.currentLatitude(),
				request.currentLongitude(),
				destination.getLatitude(),
				destination.getLongitude(),
				"출발",
				endName,
				searchOption);

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
		return create(createRequest);
	}

	/**
	 * 수거함 후보 중  목표 걸음(직선 거리 기반 추정)에 가장 가까운 곳을 선택
	 * 동점이면 현 위치에서 직선 거리가 더 짧은 수거함을 선택
	 */
	private CollectionLocation pickOptimalCollectionLocation(
			List<Long> destinationIds,
			double currentLatitude,
			double currentLongitude,
			ActivityLevel activityLevel) {
		int targetSteps = targetStepsForActivity(activityLevel);
		CollectionLocation best = null;
		int bestScore = Integer.MAX_VALUE;
		double bestDistanceMeters = Double.MAX_VALUE;

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
			if (score < bestScore || (score == bestScore && meters < bestDistanceMeters)) {
				bestScore = score;
				bestDistanceMeters = meters;
				best = loc;
			}
		}
		if (best == null) {
			throw new IllegalArgumentException("유효한 수거함 목적지가 없습니다. destinationIds를 확인하세요.");
		}
		return best;
	}

	private static int targetStepsForActivity(ActivityLevel level) {
		return switch (level) {
			case MODERATE -> 2000;
			case ACTIVE -> 4000;
			case MAXIMUM -> 6000;
		};
	}

	/**
	 * 경로의 휴식 포인트(POI) 목록 조회
	 */
	public List<PointOfInterestResponse> getRestPointsByRouteId(Long routeId) {
		return pointOfInterestRepository.findByRouteIdOrderByOrderAsc(routeId).stream()
				.map(PointOfInterestResponse::from)
				.toList();
	}
}
