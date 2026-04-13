package com.example.mediwalk_be.domain.mission.service;

import com.example.mediwalk_be.domain.mission.entity.enums.AchievementCode;
import com.example.mediwalk_be.domain.mission.repository.UserAchievementRepository;
import com.example.mediwalk_be.domain.user.entity.User;
import com.example.mediwalk_be.domain.walk.repository.DailyStepsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class AchievementProgressService {

	private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

	private final UserAchievementRepository userAchievementRepository;
	private final UserAchievementProvisioningService userAchievementProvisioningService;
	private final DailyStepsRepository dailyStepsRepository;

	@Transactional
	public void syncEnvironmentalAchievements(User user) {
		userAchievementProvisioningService.ensureDefaultAchievementsForUser(user.getId());
		int count = user.getTotalCollectionsCount() != null ? user.getTotalCollectionsCount() : 0;
		syncOne(user.getId(), AchievementCode.ENV_NOVICE, count);
		syncOne(user.getId(), AchievementCode.ENV_RELIABLE, count);
		syncOne(user.getId(), AchievementCode.ENV_GUARDIAN, count);
	}

	@Transactional
	public void syncRewardAmountAchievements(User user) {
		userAchievementProvisioningService.ensureDefaultAchievementsForUser(user.getId());
		int total = user.getTotalAccumulatedReward() != null ? user.getTotalAccumulatedReward() : 0;
		syncOne(user.getId(), AchievementCode.SAVE_THRIFTY, total);
		syncOne(user.getId(), AchievementCode.SAVE_EXPERT, total);
		syncOne(user.getId(), AchievementCode.SAVE_ASSET, total);
	}


	@Transactional
	public void syncWalkingAchievements(Long userId) {
		userAchievementProvisioningService.ensureDefaultAchievementsForUser(userId);

		long cumulative = dailyStepsRepository.sumStepsByUserId(userId);
		syncOne(userId, AchievementCode.WALK_NEWBIE, toIntSafe(cumulative));

		LocalDate todaySeoul = LocalDate.now(SEOUL);
		YearMonth ym = YearMonth.from(todaySeoul);
		LocalDate monthStart = ym.atDay(1);
		LocalDate monthEnd = ym.atEndOfMonth();
		long monthSum = dailyStepsRepository.sumStepsByUserIdAndDateBetween(userId, monthStart, monthEnd);
		syncOne(userId, AchievementCode.WALK_HEALTHY, toIntSafe(monthSum));

		syncIronMediWalker(userId, ym, toIntSafe(monthSum));
	}

	private void syncIronMediWalker(Long userId, YearMonth currentYm, int currentMonthSteps) {
		boolean ironMet = true;
		for (int i = 0; i < 3; i++) {
			YearMonth m = currentYm.minusMonths(i);
			long monthTotal = dailyStepsRepository.sumStepsByUserIdAndDateBetween(
					userId,
					m.atDay(1),
					m.atEndOfMonth());
			if (monthTotal < AchievementCode.WALK_IRON.getTargetValue()) {
				ironMet = false;
				break;
			}
		}
		final boolean ironMetFinal = ironMet;
		int target = AchievementCode.WALK_IRON.getTargetValue();
		userAchievementRepository.findByUserIdAndAchievementCode(userId, AchievementCode.WALK_IRON.getDbCode())
				.ifPresent(ua -> {
					if (ironMetFinal) {
						ua.syncAbsoluteProgress(target, target);
					} else {
						ua.syncAbsoluteProgress(currentMonthSteps, target);
					}
					userAchievementRepository.save(ua);
				});
	}

	private void syncOne(Long userId, AchievementCode code, int absoluteProgress) {
		int target = code.getTargetValue();
		userAchievementRepository.findByUserIdAndAchievementCode(userId, code.getDbCode())
				.ifPresent(ua -> {
					ua.syncAbsoluteProgress(absoluteProgress, target);
					userAchievementRepository.save(ua);
				});
	}

	private static int toIntSafe(long v) {
		if (v > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		return (int) v;
	}
}
