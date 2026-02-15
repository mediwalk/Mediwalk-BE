package com.example.mediwalk_be.repository;

import com.example.mediwalk_be.entity.Achievement;
import com.example.mediwalk_be.entity.enums.AchievementCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {

	List<Achievement> findByCategory(AchievementCategory category);
}
