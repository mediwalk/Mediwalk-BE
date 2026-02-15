package com.example.mediwalk_be.service;

import com.example.mediwalk_be.entity.CollectionLocation;
import com.example.mediwalk_be.repository.CollectionLocationRepository;
import lombok.RequiredArgsConstructor;
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
				.orElseThrow(() -> new IllegalArgumentException("CollectionLocation not found: id=" + id));
	}

	public List<CollectionLocation> findAll() {
		return collectionLocationRepository.findAll();
	}

	/**
	 * 현 위치 기준 반경 이내 목적지 조회, 거리순 정렬. (최대 개수 제한 없음)
	 * @param latitude  현재 위도
	 * @param longitude 현재 경도
	 * @param radiusMeters 반경(미터). 예: 3000 = 3km
	 */
	public List<CollectionLocation> findWithinRadiusOrderByDistance(double latitude, double longitude, int radiusMeters) {
		return collectionLocationRepository.findWithinRadiusOrderByDistance(latitude, longitude, radiusMeters);
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
