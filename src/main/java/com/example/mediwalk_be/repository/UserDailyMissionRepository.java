package com.example.mediwalk_be.repository;

import com.example.mediwalk_be.entity.UserDailyMission;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserDailyMissionRepository extends JpaRepository<UserDailyMission, Long> {

	@EntityGraph(attributePaths = {"mission", "user", "collectionLocation"})
	@Override
	Optional<UserDailyMission> findById(Long id);

	@EntityGraph(attributePaths = {"mission", "user", "collectionLocation"})
	List<UserDailyMission> findByUserIdAndMissionDate(Long userId, LocalDate missionDate);

	@EntityGraph(attributePaths = {"mission", "user", "collectionLocation"})
	Optional<UserDailyMission> findByUserIdAndMissionIdAndMissionDate(Long userId, Long missionId, LocalDate missionDate);
}
