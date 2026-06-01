package com.example.mediwalk_be.domain.walk.repository;

import com.example.mediwalk_be.domain.walk.entity.Route;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, Long> {

	@EntityGraph(attributePaths = {"user", "userDailyMission", "destination"})
	@Override
	Optional<Route> findById(Long id);

	@EntityGraph(attributePaths = {"user", "userDailyMission", "destination"})
	List<Route> findByUserIdOrderByGeneratedAtDesc(Long userId, Pageable pageable);

	@Query("""
			SELECT COALESCE(SUM(r.totalDistanceMeters), 0)
			FROM Route r
			WHERE r.user.id = :userId
			  AND r.generatedAt >= :start
			  AND r.generatedAt < :end
			""")
	long sumTotalDistanceMetersByUserIdAndGeneratedAtBetween(
			@Param("userId") Long userId,
			@Param("start") LocalDateTime start,
			@Param("end") LocalDateTime end);
}
