package com.example.mediwalk_be.domain.mission.repository;

import com.example.mediwalk_be.domain.mission.entity.Mission;
import com.example.mediwalk_be.domain.mission.entity.enums.MissionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MissionRepository extends JpaRepository<Mission, Long> {

	List<Mission> findByMissionType(MissionType missionType);
}
