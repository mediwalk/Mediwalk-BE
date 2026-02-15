package com.example.mediwalk_be.repository;

import com.example.mediwalk_be.entity.UserDailyMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserDailyMissionRepository extends JpaRepository<UserDailyMission, Long> {

	List<UserDailyMission> findByUserIdAndMissionDate(Long userId, LocalDate missionDate);

	Optional<UserDailyMission> findByUserIdAndMissionIdAndMissionDate(Long userId, Long missionId, LocalDate missionDate);
}
