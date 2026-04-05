package com.example.mediwalk_be.repository;

import com.example.mediwalk_be.entity.Route;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, Long> {

	@EntityGraph(attributePaths = {"user", "userDailyMission", "destination"})
	@Override
	Optional<Route> findById(Long id);

	@EntityGraph(attributePaths = {"user", "userDailyMission", "destination"})
	List<Route> findByUserIdOrderByGeneratedAtDesc(Long userId, Pageable pageable);
}
