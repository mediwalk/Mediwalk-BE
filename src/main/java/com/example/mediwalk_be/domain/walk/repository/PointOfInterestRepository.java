package com.example.mediwalk_be.domain.walk.repository;

import com.example.mediwalk_be.domain.walk.entity.PointOfInterest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointOfInterestRepository extends JpaRepository<PointOfInterest, Long> {

	List<PointOfInterest> findByRouteIdOrderByOrderAsc(Long routeId);
}
