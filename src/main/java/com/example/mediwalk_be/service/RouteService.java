package com.example.mediwalk_be.service;

import com.example.mediwalk_be.client.AiRouteClient;
import com.example.mediwalk_be.dto.request.CreateRouteRequest;
import com.example.mediwalk_be.dto.request.RouteGenerationRequest;
import com.example.mediwalk_be.dto.response.AiRouteGenerationResponse;
import com.example.mediwalk_be.dto.response.PointOfInterestResponse;
import com.example.mediwalk_be.entity.CollectionLocation;
import com.example.mediwalk_be.entity.Route;
import com.example.mediwalk_be.entity.User;
import com.example.mediwalk_be.entity.UserDailyMission;
import com.example.mediwalk_be.repository.CollectionLocationRepository;
import com.example.mediwalk_be.repository.PointOfInterestRepository;
import com.example.mediwalk_be.repository.RouteRepository;
import com.example.mediwalk_be.repository.UserDailyMissionRepository;
import com.example.mediwalk_be.repository.UserRepository;
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
	private final AiRouteClient aiRouteClient;

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
				.averageSlope(request.averageSlope())
				.activityLevel(request.activityLevel())
				.routePolyline(request.routePolyline())
				.greenSpaceRatio(request.greenSpaceRatio())
				.crosswalkCount(request.crosswalkCount() != null ? request.crosswalkCount() : 0)
				.isPedestrianOnly(Boolean.TRUE.equals(request.isPedestrianOnly()))
				.isNatureFriendly(Boolean.TRUE.equals(request.isNatureFriendly()))
				.hasRestPoints(Boolean.TRUE.equals(request.hasRestPoints()))
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
		AiRouteGenerationResponse aiResponse = aiRouteClient.generate(request);
		CreateRouteRequest createRequest = new CreateRouteRequest(
				request.userId(),
				null,
				aiResponse.destinationId(),
				aiResponse.startLatitude(),
				aiResponse.startLongitude(),
				aiResponse.totalDistanceMeters() != null ? aiResponse.totalDistanceMeters() : 0,
				aiResponse.estimatedWalkTimeMinutes() != null ? aiResponse.estimatedWalkTimeMinutes() : 0,
				aiResponse.estimatedSteps() != null ? aiResponse.estimatedSteps() : 0,
				aiResponse.averageSlope(),
				aiResponse.activityLevel(),
				aiResponse.routePolyline(),
				aiResponse.greenSpaceRatio(),
				aiResponse.crosswalkCount() != null ? aiResponse.crosswalkCount() : 0,
				Boolean.TRUE.equals(aiResponse.isPedestrianOnly()),
				Boolean.TRUE.equals(aiResponse.isNatureFriendly()),
				Boolean.TRUE.equals(aiResponse.hasRestPoints()));
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
