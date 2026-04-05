package com.example.mediwalk_be.domain.walk.repository;

import com.example.mediwalk_be.domain.walk.entity.DailySteps;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyStepsRepository extends JpaRepository<DailySteps, Long> {

	Optional<DailySteps> findByUserIdAndDate(Long userId, LocalDate date);
}
