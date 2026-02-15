package com.example.mediwalk_be.service;

import com.example.mediwalk_be.entity.RouteFilter;
import com.example.mediwalk_be.repository.RouteFilterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RouteFilterService {

	private final RouteFilterRepository routeFilterRepository;

	public Optional<RouteFilter> findById(Long id) {
		return routeFilterRepository.findById(id);
	}

	public RouteFilter getById(Long id) {
		return routeFilterRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("RouteFilter not found: id=" + id));
	}

	public List<RouteFilter> findAll() {
		return routeFilterRepository.findAll();
	}

	@Transactional
	public RouteFilter save(RouteFilter routeFilter) {
		return routeFilterRepository.save(routeFilter);
	}

	@Transactional
	public void deleteById(Long id) {
		routeFilterRepository.deleteById(id);
	}
}
