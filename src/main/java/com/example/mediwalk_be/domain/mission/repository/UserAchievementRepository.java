package com.example.mediwalk_be.domain.mission.repository;

import com.example.mediwalk_be.domain.mission.entity.UserAchievement;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {

	@EntityGraph(attributePaths = "achievement")
	List<UserAchievement> findByUserId(Long userId);

	@EntityGraph(attributePaths = "achievement")
	Optional<UserAchievement> findByUserIdAndAchievementId(Long userId, Long achievementId);

	@EntityGraph(attributePaths = "achievement")
	@Query("SELECT ua FROM UserAchievement ua WHERE ua.id = :id")
	Optional<UserAchievement> findByIdFetchingAchievement(@Param("id") Long id);
}
