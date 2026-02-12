package com.example.mediwalk_be.repository;

import com.example.mediwalk_be.entity.Route;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RouteRepository extends JpaRepository<Route, Long> {

	List<Route> findByUserIdOrderByGeneratedAtDesc(Long userId, Pageable pageable);
}
