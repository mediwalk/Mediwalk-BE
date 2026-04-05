package com.example.mediwalk_be.domain.mission.controller;

import com.example.mediwalk_be.domain.mission.dto.request.AddUserAchievementProgressRequest;
import com.example.mediwalk_be.domain.mission.dto.response.UserAchievementResponse;
import com.example.mediwalk_be.domain.mission.service.UserAchievementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-achievements")
@RequiredArgsConstructor
public class UserAchievementController {

	private final UserAchievementService userAchievementService;

	@GetMapping("/{id}")
	public ResponseEntity<UserAchievementResponse> findById(@PathVariable Long id) {
		return userAchievementService.findById(id)
				.map(UserAchievementResponse::from)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping(params = "userId")
	public List<UserAchievementResponse> findByUserId(@RequestParam Long userId) {
		return userAchievementService.findByUserId(userId).stream()
				.map(UserAchievementResponse::from)
				.toList();
	}

	@PostMapping("/get-or-create")
	public ResponseEntity<UserAchievementResponse> getOrCreate(
			@RequestParam Long userId,
			@RequestParam Long achievementId) {
		var ua = userAchievementService.getOrCreate(userId, achievementId);
		return ResponseEntity.status(HttpStatus.CREATED).body(UserAchievementResponse.from(ua));
	}

	@PostMapping("/{id}/add-progress")
	public ResponseEntity<UserAchievementResponse> addProgress(
			@PathVariable Long id,
			@RequestBody AddUserAchievementProgressRequest request) {
		var updated = userAchievementService.addProgress(id, request.delta());
		return ResponseEntity.ok(UserAchievementResponse.from(updated));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteById(@PathVariable Long id) {
		if (userAchievementService.findById(id).isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		userAchievementService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
