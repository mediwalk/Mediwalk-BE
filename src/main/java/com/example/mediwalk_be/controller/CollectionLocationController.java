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

	private static final int NEARBY_RADIUS_METERS = 3000;
	private static final int LIST_MAX_LIMIT = 20;

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
				? Math.min(limit, LIST_MAX_LIMIT)
				: LIST_MAX_LIMIT;
		return collectionLocationService.findWithinRadiusOrderByDistance(latitude, longitude, NEARBY_RADIUS_METERS).stream()
				.map(location -> CollectionLocationWithDistanceResponse.from(location, latitude, longitude))
				.limit(effectiveLimit)
				.toList();
	}

	/**
	 * 폐의약품 수거 장소 검색 (장소명·주소 부분 일치).
	 * 응답에 현 위치 기준 거리·도보시간·걸음수 포함.
	 */
	@GetMapping("/search")
	public List<CollectionLocationWithDistanceResponse> search(
			@RequestParam String q,
			@RequestParam double latitude,
			@RequestParam double longitude,
			@RequestParam(required = false) Integer limit) {
		int effectiveLimit = limit != null && limit > 0
				? Math.min(limit, LIST_MAX_LIMIT)
				: LIST_MAX_LIMIT;
		return collectionLocationService.searchByKeyword(q.trim(), effectiveLimit).stream()
				.map(location -> CollectionLocationWithDistanceResponse.from(location, latitude, longitude))
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
