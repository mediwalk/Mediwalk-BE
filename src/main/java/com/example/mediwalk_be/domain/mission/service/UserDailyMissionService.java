package com.example.mediwalk_be.domain.mission.service;

import com.example.mediwalk_be.domain.mission.entity.Mission;
import com.example.mediwalk_be.domain.mission.entity.UserDailyMission;
import com.example.mediwalk_be.domain.user.entity.User;
import com.example.mediwalk_be.domain.walk.entity.CollectionLocation;
import com.example.mediwalk_be.domain.mission.entity.enums.MissionStatus;
import com.example.mediwalk_be.domain.mission.entity.enums.MissionType;
import com.example.mediwalk_be.domain.walk.repository.CollectionLocationRepository;
import com.example.mediwalk_be.domain.mission.repository.MissionRepository;
import com.example.mediwalk_be.domain.mission.repository.UserDailyMissionRepository;
import com.example.mediwalk_be.domain.user.repository.UserRepository;
import com.example.mediwalk_be.domain.walk.util.DistanceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserDailyMissionService {

	private static final int NEARBY_RADIUS_METERS = 3000;

	private final UserDailyMissionRepository userDailyMissionRepository;
	private final UserRepository userRepository;
	private final MissionRepository missionRepository;
	private final CollectionLocationRepository collectionLocationRepository;

	public Optional<UserDailyMission> findById(Long id) {
		return userDailyMissionRepository.findById(id);
	}

	public UserDailyMission getById(Long id) {
		return userDailyMissionRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("UserDailyMission not found: id=" + id));
	}

	public List<UserDailyMission> findByUserIdAndMissionDate(Long userId, LocalDate missionDate) {
		return userDailyMissionRepository.findByUserIdAndMissionDate(userId, missionDate);
	}

	/**
	 * 당일 최초 진입 시: {@link MissionType#WASTE_MEDICINE_COLLECTION}, {@link MissionType#EXERCISE} 미션을 각 1개씩 생성.
	 * {@code missionDate}는 반드시 서버 기준 오늘과 같아야 함.
	 *
	 * @param latitude  있으면 가장 가까운 수거함을 폐의약품 미션 목적지로 사용 (3km 반경 우선)
	 * @param longitude 위와 동일
	 */
	@Transactional(readOnly = false)
	public void ensureTodayMissions(Long userId, LocalDate missionDate, Double latitude, Double longitude) {
		if (!missionDate.equals(LocalDate.now())) {
			throw new IllegalArgumentException("ensureCreated는 missionDate가 오늘인 경우에만 사용할 수 있습니다.");
		}
		userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found: id=" + userId));

		List<UserDailyMission> existing = userDailyMissionRepository.findByUserIdAndMissionDate(userId, missionDate);
		if (hasMissionType(existing, MissionType.WASTE_MEDICINE_COLLECTION)
				&& hasMissionType(existing, MissionType.EXERCISE)) {
			return;
		}

		CollectionLocation nearest = pickCollectionLocationForWasteMission(latitude, longitude);

		if (!hasMissionType(existing, MissionType.WASTE_MEDICINE_COLLECTION)) {
			Mission mission = pickMissionTemplate(MissionType.WASTE_MEDICINE_COLLECTION, userId, missionDate);
			Long locationId = nearest != null ? nearest.getId() : null;
			try {
				create(userId, mission.getId(), locationId, missionDate);
			} catch (DataIntegrityViolationException ignored) {
				// 동시 요청 등으로 이미 생성된 경우
			}
			existing = userDailyMissionRepository.findByUserIdAndMissionDate(userId, missionDate);
		}

		if (!hasMissionType(existing, MissionType.EXERCISE)) {
			Mission mission = pickMissionTemplate(MissionType.EXERCISE, userId, missionDate);
			try {
				create(userId, mission.getId(), null, missionDate);
			} catch (DataIntegrityViolationException ignored) {
				// 동시 요청 등으로 이미 생성된 경우
			}
		}
	}

	private boolean hasMissionType(List<UserDailyMission> list, MissionType type) {
		return list.stream().anyMatch(udm -> udm.getMission().getMissionType() == type);
	}

	private Mission pickMissionTemplate(MissionType type, Long userId, LocalDate missionDate) {
		List<Mission> missions = missionRepository.findByMissionType(type);
		if (missions.isEmpty()) {
			throw new IllegalStateException(
					"해당 타입의 미션 템플릿이 DB에 없습니다: " + type + ". missions 테이블에 데이터를 넣어주세요.");
		}
		long mod = missions.size();
		long idx = (userId + missionDate.toEpochDay()) % mod;
		if (idx < 0) {
			idx += mod;
		}
		return missions.get((int) idx);
	}

	private CollectionLocation pickCollectionLocationForWasteMission(Double latitude, Double longitude) {
		if (latitude != null && longitude != null) {
			List<CollectionLocation> nearby = collectionLocationRepository.findWithinRadiusOrderByDistance(
					latitude, longitude, NEARBY_RADIUS_METERS);
			if (!nearby.isEmpty()) {
				return nearby.get(0);
			}
		}
		return collectionLocationRepository.findAll().stream().findFirst().orElse(null);
	}

	public Optional<UserDailyMission> findByUserIdAndMissionIdAndMissionDate(Long userId, Long missionId, LocalDate missionDate) {
		return userDailyMissionRepository.findByUserIdAndMissionIdAndMissionDate(userId, missionId, missionDate);
	}

	@Transactional
	public UserDailyMission save(UserDailyMission userDailyMission) {
		return userDailyMissionRepository.save(userDailyMission);
	}

	@Transactional
	public UserDailyMission create(Long userId, Long missionId, Long collectionLocationId, LocalDate missionDate) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found: id=" + userId));
		Mission mission = missionRepository.findById(missionId)
				.orElseThrow(() -> new IllegalArgumentException("Mission not found: id=" + missionId));
		CollectionLocation collectionLocation = collectionLocationId != null
				? collectionLocationRepository.findById(collectionLocationId).orElse(null)
				: null;

		UserDailyMission udm = UserDailyMission.builder()
				.user(user)
				.mission(mission)
				.collectionLocation(collectionLocation)
				.missionDate(missionDate)
				.status(MissionStatus.PENDING)
				.build();
		return userDailyMissionRepository.save(udm);
	}

	@Transactional
	public UserDailyMission complete(Long id, Integer earnedReward, Double currentLatitude, Double currentLongitude) {
		UserDailyMission udm = getById(id);
		
		// 목적지(수거함 위치)가 있는 경우 20m 반경 검증
		if (udm.getCollectionLocation() != null) {
			if (currentLatitude == null || currentLongitude == null) {
				throw new IllegalArgumentException("미션 완료를 위해 현재 위치 정보가 필요합니다.");
			}
			
			CollectionLocation destination = udm.getCollectionLocation();
			int activationRadius = destination.getActivationRadius() != null 
					? destination.getActivationRadius() 
					: 20; // 기본값 20m
			
			boolean isWithinRadius = DistanceUtil.isWithinRadius(
					currentLatitude,
					currentLongitude,
					destination.getLatitude(),
					destination.getLongitude(),
					activationRadius
			);
			
			if (!isWithinRadius) {
				double distance = DistanceUtil.calculateDistanceMeters(
						currentLatitude,
						currentLongitude,
						destination.getLatitude(),
						destination.getLongitude()
				);
				throw new IllegalArgumentException(
						String.format("목적지로부터 %d미터 이내에 있어야 합니다. 현재 거리: %.1f미터", activationRadius, distance)
				);
			}
		}
		
		udm.complete(LocalDateTime.now(), earnedReward);
		return userDailyMissionRepository.save(udm);
	}

	@Transactional
	public void deleteById(Long id) {
		userDailyMissionRepository.deleteById(id);
	}
}
