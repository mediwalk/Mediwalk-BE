package com.example.mediwalk_be.repository;

import com.example.mediwalk_be.entity.PointOfInterest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointOfInterestRepository extends JpaRepository<PointOfInterest, Long> {

	List<PointOfInterest> findByRouteIdOrderByOrderAsc(Long routeId);
}
