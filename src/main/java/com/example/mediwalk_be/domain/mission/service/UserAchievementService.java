package com.example.mediwalk_be.domain.mission.service;

import com.example.mediwalk_be.domain.mission.entity.Achievement;
import com.example.mediwalk_be.domain.user.entity.User;
import com.example.mediwalk_be.domain.mission.entity.UserAchievement;
import com.example.mediwalk_be.domain.mission.repository.AchievementRepository;
import com.example.mediwalk_be.domain.mission.repository.UserAchievementRepository;
import com.example.mediwalk_be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAchievementService {

	private final UserAchievementRepository userAchievementRepository;
	private final UserRepository userRepository;
	private final AchievementRepository achievementRepository;

	public Optional<UserAchievement> findById(Long id) {
		return userAchievementRepository.findByIdFetchingAchievement(id);
	}

	public UserAchievement getById(Long id) {
		return userAchievementRepository.findByIdFetchingAchievement(id)
				.orElseThrow(() -> new IllegalArgumentException("UserAchievement not found: id=" + id));
	}

	public List<UserAchievement> findByUserId(Long userId) {
		return userAchievementRepository.findByUserId(userId);
	}

	public Optional<UserAchievement> findByUserIdAndAchievementId(Long userId, Long achievementId) {
		return userAchievementRepository.findByUserIdAndAchievementId(userId, achievementId);
	}

	@Transactional
	public UserAchievement save(UserAchievement userAchievement) {
		return userAchievementRepository.save(userAchievement);
	}

	@Transactional
	public UserAchievement getOrCreate(Long userId, Long achievementId) {
		Optional<UserAchievement> existing = userAchievementRepository.findByUserIdAndAchievementId(userId, achievementId);
		if (existing.isPresent()) {
			return existing.get();
		}
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found: id=" + userId));
		Achievement achievement = achievementRepository.findById(achievementId)
				.orElseThrow(() -> new IllegalArgumentException("Achievement not found: id=" + achievementId));
		UserAchievement ua = UserAchievement.builder()
				.user(user)
				.achievement(achievement)
				.currentProgress(0)
				.isAchieved(false)
				.build();
		return userAchievementRepository.save(ua);
	}

	@Transactional
	public UserAchievement addProgress(Long userAchievementId, int delta) {
		UserAchievement ua = getById(userAchievementId);
		int targetValue = ua.getAchievement().getTargetValue();
		ua.addProgress(delta, targetValue);
		return userAchievementRepository.save(ua);
	}

	@Transactional
	public void deleteById(Long id) {
		userAchievementRepository.deleteById(id);
	}
}
