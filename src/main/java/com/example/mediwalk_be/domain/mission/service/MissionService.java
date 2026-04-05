package com.example.mediwalk_be.domain.mission.service;

import com.example.mediwalk_be.domain.mission.entity.Mission;
import com.example.mediwalk_be.domain.mission.entity.enums.MissionType;
import com.example.mediwalk_be.domain.mission.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MissionService {

	private final MissionRepository missionRepository;

	public Optional<Mission> findById(Long id) {
		return missionRepository.findById(id);
	}

	public Mission getById(Long id) {
		return missionRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Mission not found: id=" + id));
	}

	public List<Mission> findAll() {
		return missionRepository.findAll();
	}

	public List<Mission> findByMissionType(MissionType missionType) {
		return missionRepository.findByMissionType(missionType);
	}

	@Transactional
	public Mission save(Mission mission) {
		return missionRepository.save(mission);
	}

	@Transactional
	public void deleteById(Long id) {
		missionRepository.deleteById(id);
	}
}
