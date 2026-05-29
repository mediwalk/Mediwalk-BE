package com.example.mediwalk_be.domain.reward.service;

import com.example.mediwalk_be.domain.reward.dto.response.EventCreateResponse;
import com.example.mediwalk_be.domain.reward.entity.Event;
import com.example.mediwalk_be.domain.reward.entity.RewardTransaction;
import com.example.mediwalk_be.domain.user.entity.User;
import com.example.mediwalk_be.domain.walk.entity.CollectionLocation;
import com.example.mediwalk_be.domain.walk.entity.Route;
import com.example.mediwalk_be.domain.reward.dto.request.CreateEventRequest;
import com.example.mediwalk_be.domain.reward.entity.enums.EventType;
import com.example.mediwalk_be.domain.reward.entity.enums.RewardTransactionType;
import com.example.mediwalk_be.domain.walk.repository.CollectionLocationRepository;
import com.example.mediwalk_be.domain.reward.repository.EventRepository;
import com.example.mediwalk_be.domain.walk.repository.DailyStepsRepository;
import com.example.mediwalk_be.domain.walk.repository.RouteRepository;
import com.example.mediwalk_be.domain.mission.entity.UserDailyMission;
import com.example.mediwalk_be.domain.mission.entity.enums.MissionStatus;
import com.example.mediwalk_be.domain.mission.repository.UserAchievementRepository;
import com.example.mediwalk_be.domain.mission.service.AchievementProgressService;
import com.example.mediwalk_be.domain.mission.service.UserDailyMissionService;
import com.example.mediwalk_be.domain.reward.repository.RewardTransactionRepository;
import com.example.mediwalk_be.domain.user.repository.UserRepository;
import com.example.mediwalk_be.domain.walk.util.DistanceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

	private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
	private static final double METERS_PER_STEP = 0.7;

	private final EventRepository eventRepository;
	private final UserRepository userRepository;
	private final CollectionLocationRepository collectionLocationRepository;
	private final RouteRepository routeRepository;
	private final DailyStepsRepository dailyStepsRepository;
	private final UserAchievementRepository userAchievementRepository;
	private final RewardTransactionRepository rewardTransactionRepository;
	private final AchievementProgressService achievementProgressService;
	private final UserDailyMissionService userDailyMissionService;

	public Optional<Event> findById(Long id) {
		return eventRepository.findById(id);
	}

	public Event getById(Long id) {
		return eventRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Event not found: id=" + id));
	}

	public List<Event> findByUserIdOrderByEventDateTimeDesc(Long userId, Pageable pageable) {
		return eventRepository.findByUserIdOrderByEventDateTimeDesc(userId, pageable);
	}

	public List<Event> findByUserIdWithOptionalEventType(Long userId, EventType eventType, Pageable pageable) {
		if (eventType != null) {
			return eventRepository.findByUserIdAndEventTypeOrderByEventDateTimeDesc(userId, eventType, pageable);
		}
		return eventRepository.findByUserIdOrderByEventDateTimeDesc(userId, pageable);
	}

	public List<Event> findByUserIdAndEventDateTimeBetween(Long userId, LocalDateTime start, LocalDateTime end) {
		return eventRepository.findByUserIdAndEventDateTimeBetween(userId, start, end);
	}

	public long countByUserIdAndEventTypeAndEventDateTimeBetween(Long userId, EventType eventType, LocalDateTime start, LocalDateTime end) {
		return eventRepository.countByUserIdAndEventTypeAndEventDateTimeBetween(userId, eventType, start, end);
	}

	@Transactional
	public Event save(Event event) {
		return eventRepository.save(event);
	}


	@Transactional
	public EventCreateResponse create(CreateEventRequest request) {
		User user = userRepository.findById(request.userId())
				.orElseThrow(() -> new IllegalArgumentException("User not found: id=" + request.userId()));
		CollectionLocation collectionLocation = request.collectionLocationId() != null
				? collectionLocationRepository.findById(request.collectionLocationId()).orElse(null)
				: null;
		Route route = request.routeId() != null
				? routeRepository.findById(request.routeId()).orElse(null)
				: null;

		// 운동 경로 인증 또는 폐의약품 수거 인증 시 20m 반경 검증
		if (collectionLocation != null && request.currentLatitude() != null && request.currentLongitude() != null) {
			int activationRadius = collectionLocation.getActivationRadius() != null 
					? collectionLocation.getActivationRadius() 
					: 20;
			
			boolean isWithinRadius = DistanceUtil.isWithinRadius(
					request.currentLatitude(),
					request.currentLongitude(),
					collectionLocation.getLatitude(),
					collectionLocation.getLongitude(),
					activationRadius
			);
			
			if (!isWithinRadius) {
				double distance = DistanceUtil.calculateDistanceMeters(
						request.currentLatitude(),
						request.currentLongitude(),
						collectionLocation.getLatitude(),
						collectionLocation.getLongitude()
				);
				throw new IllegalArgumentException(
						String.format("목적지로부터 %d미터 이내에 있어야 합니다. 현재 거리: %.1f미터", activationRadius, distance)
				);
			}
		}

		int rewardAmount = request.rewardAmount() != null ? request.rewardAmount() : 0;

		Event event = Event.builder()
				.user(user)
				.eventType(request.eventType())
				.title(request.title() != null ? request.title() : request.eventType().name())
				.rewardAmount(rewardAmount)
				.eventDateTime(request.eventDateTime() != null ? request.eventDateTime() : LocalDateTime.now())
				.locationName(request.locationName())
				.collectionLocation(collectionLocation)
				.imageUrl(request.imageUrl())
				.route(route)
				.build();
		event = eventRepository.save(event);

		if (request.eventType() == EventType.MEDICINE_COLLECTION) {
			user.incrementTotalCollectionsCount();
		}
		if (rewardAmount > 0) {
			user.addAccumulatedReward(rewardAmount);
			RewardTransaction accumulation = RewardTransaction.builder()
					.user(user)
					.event(event)
					.amount(rewardAmount)
					.transactionType(RewardTransactionType.ACCUMULATION)
					.transactionDate(event.getEventDateTime())
					.description(event.getTitle())
					.build();
			rewardTransactionRepository.save(accumulation);
		}
		if (request.eventType() == EventType.MEDICINE_COLLECTION) {
			achievementProgressService.syncEnvironmentalAchievements(user);
		}
		if (rewardAmount > 0) {
			achievementProgressService.syncRewardAmountAchievements(user);
		}

		Long completedMissionId = null;
		MissionStatus completedMissionStatus = null;
		if (request.userDailyMissionId() != null) {
			UserDailyMission completedMission = completeLinkedDailyMission(request, rewardAmount);
			completedMissionId = completedMission.getId();
			completedMissionStatus = completedMission.getStatus();
		}

		int totalAccumulatedReward = user.getTotalAccumulatedReward() != null ? user.getTotalAccumulatedReward() : 0;
		int todayWalkingDistanceMeters = resolveTodayWalkingDistanceMeters(user.getId());
		String todayAchievementName = resolveTodayAchievementName(user.getId());

		return EventCreateResponse.of(
				event,
				totalAccumulatedReward,
				todayWalkingDistanceMeters,
				todayAchievementName,
				completedMissionId,
				completedMissionStatus
		);
	}

	private UserDailyMission completeLinkedDailyMission(CreateEventRequest request, int rewardAmount) {
		UserDailyMission mission = userDailyMissionService.getById(request.userDailyMissionId());
		if (!mission.getUser().getId().equals(request.userId())) {
			throw new IllegalArgumentException("userDailyMissionId가 userId와 일치하지 않습니다.");
		}
		if (mission.getStatus() == MissionStatus.COMPLETED) {
			throw new IllegalArgumentException("이미 완료된 일일 미션입니다: id=" + mission.getId());
		}
		if (request.collectionLocationId() != null
				&& mission.getCollectionLocation() != null
				&& !request.collectionLocationId().equals(mission.getCollectionLocation().getId())) {
			throw new IllegalArgumentException("collectionLocationId가 일일 미션 목적지와 일치하지 않습니다.");
		}
		return userDailyMissionService.complete(
				mission.getId(),
				rewardAmount,
				request.currentLatitude(),
				request.currentLongitude()
		);
	}

	private int resolveTodayWalkingDistanceMeters(Long userId) {
		LocalDate today = LocalDate.now(SEOUL);
		LocalDateTime dayStart = today.atStartOfDay();
		LocalDateTime dayEnd = today.plusDays(1).atStartOfDay();

		long routeDistance = routeRepository.sumTotalDistanceMetersByUserIdAndGeneratedAtBetween(
				userId, dayStart, dayEnd);
		if (routeDistance > 0) {
			return toIntSafe(routeDistance);
		}

		return dailyStepsRepository.findByUserIdAndDate(userId, today)
				.map(ds -> toIntSafe(Math.round(ds.getStepsCount() * METERS_PER_STEP)))
				.orElse(0);
	}

	private String resolveTodayAchievementName(Long userId) {
		LocalDate today = LocalDate.now(SEOUL);
		LocalDateTime dayStart = today.atStartOfDay();
		LocalDateTime dayEnd = today.plusDays(1).atStartOfDay();

		return userAchievementRepository
				.findAchievedByUserIdAndAchievedDateBetweenOrderByAchievedDateDesc(userId, dayStart, dayEnd)
				.stream()
				.findFirst()
				.map(ua -> ua.getAchievement().getName())
				.orElse(null);
	}

	private static int toIntSafe(long value) {
		if (value > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		return (int) value;
	}

	@Transactional
	public void deleteById(Long id) {
		eventRepository.deleteById(id);
	}
}
