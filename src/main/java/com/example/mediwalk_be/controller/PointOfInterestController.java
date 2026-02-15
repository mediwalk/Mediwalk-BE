package com.example.mediwalk_be.controller;

import com.example.mediwalk_be.dto.response.PointOfInterestResponse;
import com.example.mediwalk_be.service.PointOfInterestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/points-of-interest")
@RequiredArgsConstructor
public class PointOfInterestController {

	private final PointOfInterestService pointOfInterestService;

	@GetMapping("/{id}")
	public ResponseEntity<PointOfInterestResponse> findById(@PathVariable Long id) {
		return pointOfInterestService.findById(id)
				.map(PointOfInterestResponse::from)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping(params = "routeId")
	public List<PointOfInterestResponse> findByRouteId(@RequestParam Long routeId) {
		return pointOfInterestService.findByRouteIdOrderByOrderAsc(routeId).stream()
				.map(PointOfInterestResponse::from)
				.toList();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteById(@PathVariable Long id) {
		if (pointOfInterestService.findById(id).isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		pointOfInterestService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
