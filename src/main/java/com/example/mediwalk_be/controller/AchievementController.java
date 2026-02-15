package com.example.mediwalk_be.controller;

import com.example.mediwalk_be.dto.request.CreateAchievementRequest;
import com.example.mediwalk_be.dto.response.AchievementResponse;
import com.example.mediwalk_be.entity.Achievement;
import com.example.mediwalk_be.service.AchievementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
	public List<AchievementResponse> findByCategory(@RequestParam com.example.mediwalk_be.entity.enums.AchievementCategory category) {
		return achievementService.findByCategory(category).stream()
				.map(AchievementResponse::from)
				.toList();
	}

	@PostMapping
	public ResponseEntity<AchievementResponse> create(@RequestBody CreateAchievementRequest request) {
		Achievement entity = Achievement.builder()
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
