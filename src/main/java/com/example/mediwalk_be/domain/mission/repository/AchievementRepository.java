package com.example.mediwalk_be.domain.mission.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mediwalk_be.domain.mission.entity.Achievement;
import com.example.mediwalk_be.domain.mission.entity.enums.AchievementCategory;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {

	List<Achievement> findByCategory(AchievementCategory category);

	Optional<Achievement> findByCode(String code);

	List<Achievement> findByCodeIn(Collection<String> codes);
}
