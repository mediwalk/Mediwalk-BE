package com.example.mediwalk_be.service;

import com.example.mediwalk_be.entity.DailySteps;
import com.example.mediwalk_be.entity.User;
import com.example.mediwalk_be.repository.DailyStepsRepository;
import com.example.mediwalk_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyStepsService {

	private final DailyStepsRepository dailyStepsRepository;
	private final UserRepository userRepository;

	public Optional<DailySteps> findById(Long id) {
		return dailyStepsRepository.findById(id);
	}

	public DailySteps getById(Long id) {
		return dailyStepsRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("DailySteps not found: id=" + id));
	}

	public Optional<DailySteps> findByUserIdAndDate(Long userId, LocalDate date) {
		return dailyStepsRepository.findByUserIdAndDate(userId, date);
	}

	@Transactional
	public DailySteps save(DailySteps dailySteps) {
		return dailyStepsRepository.save(dailySteps);
	}

	@Transactional
	public DailySteps getOrCreate(Long userId, LocalDate date) {
		return dailyStepsRepository.findByUserIdAndDate(userId, date)
				.orElseGet(() -> {
					User user = userRepository.findById(userId)
							.orElseThrow(() -> new IllegalArgumentException("User not found: id=" + userId));
					DailySteps ds = DailySteps.builder()
							.user(user)
							.date(date)
							.stepsCount(0)
							.build();
					return dailyStepsRepository.save(ds);
				});
	}

	@Transactional
	public DailySteps addSteps(Long dailyStepsId, int count) {
		DailySteps ds = getById(dailyStepsId);
		ds.addSteps(count);
		return dailyStepsRepository.save(ds);
	}

	@Transactional
	public void deleteById(Long id) {
		dailyStepsRepository.deleteById(id);
	}
}
