package com.example.mediwalk_be.service;

import com.example.mediwalk_be.entity.*;
import com.example.mediwalk_be.entity.enums.MissionStatus;
import com.example.mediwalk_be.repository.CollectionLocationRepository;
import com.example.mediwalk_be.repository.MissionRepository;
import com.example.mediwalk_be.repository.UserDailyMissionRepository;
import com.example.mediwalk_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
	public UserDailyMission complete(Long id, Integer earnedReward) {
		UserDailyMission udm = getById(id);
		udm.complete(LocalDateTime.now(), earnedReward);
		return userDailyMissionRepository.save(udm);
	}

	@Transactional
	public void deleteById(Long id) {
		userDailyMissionRepository.deleteById(id);
	}
}
