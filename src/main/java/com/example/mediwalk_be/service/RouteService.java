package com.example.mediwalk_be.service;

import com.example.mediwalk_be.entity.Route;
import com.example.mediwalk_be.repository.CollectionLocationRepository;
import com.example.mediwalk_be.repository.RouteRepository;
import com.example.mediwalk_be.repository.UserDailyMissionRepository;
import com.example.mediwalk_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RouteService {

	private final RouteRepository routeRepository;
	private final UserRepository userRepository;
	private final UserDailyMissionRepository userDailyMissionRepository;
	private final CollectionLocationRepository collectionLocationRepository;

	public Optional<Route> findById(Long id) {
		return routeRepository.findById(id);
	}

	public Route getById(Long id) {
		return routeRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Route not found: id=" + id));
	}

	public List<Route> findByUserIdOrderByGeneratedAtDesc(Long userId, Pageable pageable) {
		return routeRepository.findByUserIdOrderByGeneratedAtDesc(userId, pageable);
	}

	@Transactional
	public Route save(Route route) {
		return routeRepository.save(route);
	}

	@Transactional
	public void deleteById(Long id) {
		routeRepository.deleteById(id);
	}
}
