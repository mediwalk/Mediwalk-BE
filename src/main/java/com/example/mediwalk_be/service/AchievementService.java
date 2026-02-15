package com.example.mediwalk_be.service;

import com.example.mediwalk_be.entity.Achievement;
import com.example.mediwalk_be.entity.enums.AchievementCategory;
import com.example.mediwalk_be.repository.AchievementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AchievementService {

	private final AchievementRepository achievementRepository;

	public Optional<Achievement> findById(Long id) {
		return achievementRepository.findById(id);
	}

	public Achievement getById(Long id) {
		return achievementRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Achievement not found: id=" + id));
	}

	public List<Achievement> findAll() {
		return achievementRepository.findAll();
	}

	public List<Achievement> findByCategory(AchievementCategory category) {
		return achievementRepository.findByCategory(category);
	}

	@Transactional
	public Achievement save(Achievement achievement) {
		return achievementRepository.save(achievement);
	}

	@Transactional
	public void deleteById(Long id) {
		achievementRepository.deleteById(id);
	}
}
