package com.example.mediwalk_be.domain.mission.service;

import com.example.mediwalk_be.domain.mission.entity.Achievement;
import com.example.mediwalk_be.domain.mission.entity.UserAchievement;
import com.example.mediwalk_be.domain.mission.entity.enums.AchievementCode;
import com.example.mediwalk_be.domain.mission.repository.AchievementRepository;
import com.example.mediwalk_be.domain.mission.repository.UserAchievementRepository;
import com.example.mediwalk_be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserAchievementProvisioningService {

	private final UserRepository userRepository;
	private final AchievementRepository achievementRepository;
	private final UserAchievementRepository userAchievementRepository;

	@Transactional
	public void ensureDefaultAchievementsForUser(Long userId) {
		var user = userRepository.findById(userId).orElse(null);
		if (user == null) {
			return;
		}
		List<String> codes = Arrays.stream(AchievementCode.values())
				.map(AchievementCode::getDbCode)
				.toList();
		List<Achievement> rows = achievementRepository.findByCodeIn(codes);
		if (rows.size() < AchievementCode.values().length) {
			return;
		}
		Map<String, Achievement> byCode = rows.stream()
				.collect(Collectors.toMap(Achievement::getCode, a -> a, (a, b) -> a));
		for (AchievementCode ac : AchievementCode.values()) {
			Achievement achievement = byCode.get(ac.getDbCode());
			if (achievement == null) {
				continue;
			}
			if (userAchievementRepository.existsByUserIdAndAchievementId(userId, achievement.getId())) {
				continue;
			}
			userAchievementRepository.save(UserAchievement.builder()
					.user(user)
					.achievement(achievement)
					.currentProgress(0)
					.isAchieved(false)
					.build());
		}
	}
}
