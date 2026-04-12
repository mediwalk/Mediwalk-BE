package com.example.mediwalk_be.domain.mission.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.mediwalk_be.domain.mission.dto.request.CreateAchievementRequest;
import com.example.mediwalk_be.domain.mission.dto.response.AchievementResponse;
import com.example.mediwalk_be.domain.mission.entity.Achievement;
import com.example.mediwalk_be.domain.mission.entity.enums.AchievementCategory;
import com.example.mediwalk_be.domain.mission.service.AchievementService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
public class AchievementController {

	private final AchievementService achievementService;

	@GetMapping
	public List<AchievementResponse> findAll() {
		return achievementService.findAll().stream()
				.map(AchievementResponse::from)
				.toList();
	}

	@GetMapping("/{id}")
	public ResponseEntity<AchievementResponse> findById(@PathVariable Long id) {
		return achievementService.findById(id)
				.map(AchievementResponse::from)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping(params = "category")
	public List<AchievementResponse> findByCategory(@RequestParam AchievementCategory category) {
		return achievementService.findByCategory(category).stream()
				.map(AchievementResponse::from)
				.toList();
	}

	@PostMapping
	public ResponseEntity<AchievementResponse> create(@Valid @RequestBody CreateAchievementRequest request) {
		Achievement entity = Achievement.builder()
				.code(request.code())
				.name(request.name())
				.description(request.description())
				.category(request.category())
				.targetValue(request.targetValue())
				.unit(request.unit())
				.iconType(request.iconType())
				.build();
		Achievement saved = achievementService.save(entity);
		return ResponseEntity.status(HttpStatus.CREATED).body(AchievementResponse.from(saved));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteById(@PathVariable Long id) {
		if (achievementService.findById(id).isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		achievementService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
