package com.example.mediwalk_be.domain.walk.repository;

import com.example.mediwalk_be.domain.walk.entity.DailySteps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyStepsRepository extends JpaRepository<DailySteps, Long> {

	Optional<DailySteps> findByUserIdAndDate(Long userId, LocalDate date);

	@Query("SELECT COALESCE(SUM(d.stepsCount), 0) FROM DailySteps d WHERE d.user.id = :userId")
	long sumStepsByUserId(@Param("userId") Long userId);

	@Query("SELECT COALESCE(SUM(d.stepsCount), 0) FROM DailySteps d WHERE d.user.id = :userId AND d.date BETWEEN :start AND :end")
	long sumStepsByUserIdAndDateBetween(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);
}
