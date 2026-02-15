package com.example.mediwalk_be.service;

import com.example.mediwalk_be.entity.PointOfInterest;
import com.example.mediwalk_be.repository.PointOfInterestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointOfInterestService {

	private final PointOfInterestRepository pointOfInterestRepository;

	public Optional<PointOfInterest> findById(Long id) {
		return pointOfInterestRepository.findById(id);
	}

	public PointOfInterest getById(Long id) {
		return pointOfInterestRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("PointOfInterest not found: id=" + id));
	}

	public List<PointOfInterest> findByRouteIdOrderByOrderAsc(Long routeId) {
		return pointOfInterestRepository.findByRouteIdOrderByOrderAsc(routeId);
	}

	@Transactional
	public PointOfInterest save(PointOfInterest pointOfInterest) {
		return pointOfInterestRepository.save(pointOfInterest);
	}

	@Transactional
	public void deleteById(Long id) {
		pointOfInterestRepository.deleteById(id);
	}
}
