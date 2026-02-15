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

	@Transactional
	public CollectionLocation save(CollectionLocation collectionLocation) {
		return collectionLocationRepository.save(collectionLocation);
	}

	@Transactional
	public void deleteById(Long id) {
		collectionLocationRepository.deleteById(id);
	}
}
