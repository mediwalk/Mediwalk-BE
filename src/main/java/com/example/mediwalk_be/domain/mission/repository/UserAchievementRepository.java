package com.example.mediwalk_be.domain.mission.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.mediwalk_be.domain.mission.entity.UserAchievement;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {

	@EntityGraph(attributePaths = "achievement")
	List<UserAchievement> findByUserId(Long userId);

	@EntityGraph(attributePaths = "achievement")
	Optional<UserAchievement> findByUserIdAndAchievementId(Long userId, Long achievementId);

	boolean existsByUserIdAndAchievementId(Long userId, Long achievementId);

	@Query("SELECT ua FROM UserAchievement ua JOIN ua.achievement a WHERE ua.user.id = :userId AND a.code = :code")
	Optional<UserAchievement> findByUserIdAndAchievementCode(@Param("userId") Long userId, @Param("code") String code);

	@EntityGraph(attributePaths = "achievement")
	@Query("SELECT ua FROM UserAchievement ua WHERE ua.id = :id")
	Optional<UserAchievement> findByIdFetchingAchievement(@Param("id") Long id);
}
