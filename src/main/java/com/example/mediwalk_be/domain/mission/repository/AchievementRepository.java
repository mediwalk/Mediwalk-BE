package com.example.mediwalk_be.domain.mission.repository;

import com.example.mediwalk_be.domain.mission.entity.Achievement;
import com.example.mediwalk_be.domain.mission.entity.enums.AchievementCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {

	List<Achievement> findByCategory(AchievementCategory category);
}
