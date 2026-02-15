package com.example.mediwalk_be.repository;

import com.example.mediwalk_be.entity.Mission;
import com.example.mediwalk_be.entity.enums.MissionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MissionRepository extends JpaRepository<Mission, Long> {

	List<Mission> findByMissionType(MissionType missionType);
}
