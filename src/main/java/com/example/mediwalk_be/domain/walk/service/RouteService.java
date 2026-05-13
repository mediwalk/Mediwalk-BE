package com.example.mediwalk_be.domain.walk.service;

import com.example.mediwalk_be.domain.walk.dto.request.CreateRouteRequest;
import com.example.mediwalk_be.domain.walk.dto.request.RouteFilterRequest;
import com.example.mediwalk_be.domain.walk.dto.request.RouteGenerationRequest;
import com.example.mediwalk_be.domain.walk.dto.response.PointOfInterestResponse;
import com.example.mediwalk_be.domain.walk.entity.CollectionLocation;
import com.example.mediwalk_be.domain.walk.entity.Route;
import com.example.mediwalk_be.domain.user.entity.User;
import com.example.mediwalk_be.domain.mission.entity.UserDailyMission;
import com.example.mediwalk_be.domain.walk.repository.CollectionLocationRepository;
import com.example.mediwalk_be.domain.walk.repository.PointOfInterestRepository;
import com.example.mediwalk_be.domain.walk.repository.RouteRepository;
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
		CollectionLocation destination = collectionLocationRepository
				.findById(request.destinationIds().get(0))
				.orElseThrow(() -> new IllegalArgumentException(
						"CollectionLocation not found: id=" + request.destinationIds().get(0)));

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

		RouteFilterRequest f = request.filter();
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
	 * 경로의 휴식 포인트(POI) 목록 조회
	 */
	public List<PointOfInterestResponse> getRestPointsByRouteId(Long routeId) {
		return pointOfInterestRepository.findByRouteIdOrderByOrderAsc(routeId).stream()
				.map(PointOfInterestResponse::from)
				.toList();
	}
}
