package com.example.mediwalk_be.domain.walk.service;

import com.example.mediwalk_be.domain.walk.dto.response.DestinationProximityResponse;
import com.example.mediwalk_be.domain.walk.entity.CollectionLocation;
import com.example.mediwalk_be.domain.walk.repository.CollectionLocationRepository;
import com.example.mediwalk_be.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollectionLocationService {

	private final CollectionLocationRepository collectionLocationRepository;

	public Optional<CollectionLocation> findById(Long id) {
		return collectionLocationRepository.findById(id);
	}

	public CollectionLocation getById(Long id) {
		return collectionLocationRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("CollectionLocation not found: id=" + id));
	}

	public DestinationProximityResponse checkDestinationProximity(
			Long id,
			double currentLatitude,
			double currentLongitude) {
		CollectionLocation destination = getById(id);
		return DestinationProximityResponse.from(destination, currentLatitude, currentLongitude);
	}

	public List<CollectionLocation> findAll() {
		return collectionLocationRepository.findAll();
	}

	public List<CollectionLocation> findWithinRadiusOrderByDistance(double latitude, double longitude, int radiusMeters) {
		return collectionLocationRepository.findWithinRadiusOrderByDistance(latitude, longitude, radiusMeters);
	}

	public List<CollectionLocation> searchByKeyword(String keyword, int maxResults) {
		if (keyword == null || keyword.isBlank()) {
			return List.of();
		}
		String trimmed = keyword.trim();
		return collectionLocationRepository.findDistinctByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(
				trimmed,
				trimmed,
				PageRequest.of(0, Math.min(maxResults, 20))
		);
	}

	@Transactional
	public CollectionLocation save(CollectionLocation collectionLocation) {
		return collectionLocationRepository.save(collectionLocation);
	}

	@Transactional
	public void deleteById(Long id) {
		collectionLocationRepository.deleteById(id);
	}
}
