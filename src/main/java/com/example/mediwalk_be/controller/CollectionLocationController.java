package com.example.mediwalk_be.controller;

import com.example.mediwalk_be.dto.request.CreateCollectionLocationRequest;
import com.example.mediwalk_be.dto.response.CollectionLocationResponse;
import com.example.mediwalk_be.dto.response.CollectionLocationWithDistanceResponse;
import com.example.mediwalk_be.entity.CollectionLocation;
import com.example.mediwalk_be.service.CollectionLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collection-locations")
@RequiredArgsConstructor
public class CollectionLocationController {

	private static final int NEARBY_RADIUS_METERS = 3000; // 3km 통일
	private static final int NEARBY_MAX_LIMIT = 20; // 최대 20개 이내 조회

	private final CollectionLocationService collectionLocationService;

	@GetMapping
	public List<CollectionLocationResponse> findAll() {
		return collectionLocationService.findAll().stream()
				.map(CollectionLocationResponse::from)
				.toList();
	}

	@GetMapping("/nearby")
	public List<CollectionLocationWithDistanceResponse> findNearby(
			@RequestParam double latitude,
			@RequestParam double longitude,
			@RequestParam(required = false) Integer limit) {
		int effectiveLimit = limit != null && limit > 0
				? Math.min(limit, NEARBY_MAX_LIMIT)
				: NEARBY_MAX_LIMIT;
		return collectionLocationService.findWithinRadiusOrderByDistance(latitude, longitude, NEARBY_RADIUS_METERS).stream()
				.map(location -> CollectionLocationWithDistanceResponse.from(location, latitude, longitude))
				.limit(effectiveLimit)
				.toList();
	}

	@GetMapping("/{id}")
	public ResponseEntity<CollectionLocationResponse> findById(@PathVariable Long id) {
		return collectionLocationService.findById(id)
				.map(CollectionLocationResponse::from)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<CollectionLocationResponse> create(@RequestBody CreateCollectionLocationRequest request) {
		CollectionLocation entity = CollectionLocation.builder()
				.name(request.name())
				.address(request.address())
				.latitude(request.latitude())
				.longitude(request.longitude())
				.type(request.type())
				.baseRewardAmount(request.baseRewardAmount() != null ? request.baseRewardAmount() : 3000)
				.activationRadius(request.activationRadius() != null ? request.activationRadius() : 20)
				.build();
		CollectionLocation saved = collectionLocationService.save(entity);
		return ResponseEntity.status(HttpStatus.CREATED).body(CollectionLocationResponse.from(saved));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteById(@PathVariable Long id) {
		if (collectionLocationService.findById(id).isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		collectionLocationService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
