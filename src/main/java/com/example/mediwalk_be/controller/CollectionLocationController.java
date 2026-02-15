package com.example.mediwalk_be.controller;

import com.example.mediwalk_be.dto.request.CreateCollectionLocationRequest;
import com.example.mediwalk_be.dto.response.CollectionLocationResponse;
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

	private final CollectionLocationService collectionLocationService;

	@GetMapping
	public List<CollectionLocationResponse> findAll() {
		return collectionLocationService.findAll().stream()
				.map(CollectionLocationResponse::from)
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
