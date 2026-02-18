package com.example.mediwalk_be.controller;

import com.example.mediwalk_be.dto.request.CompleteUserDailyMissionRequest;
import com.example.mediwalk_be.dto.request.CreateUserDailyMissionRequest;
import com.example.mediwalk_be.dto.response.UserDailyMissionResponse;
import com.example.mediwalk_be.service.UserDailyMissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/user-daily-missions")
@RequiredArgsConstructor
public class UserDailyMissionController {

	private final UserDailyMissionService userDailyMissionService;

	@GetMapping("/{id}")
	public ResponseEntity<UserDailyMissionResponse> findById(
			@PathVariable Long id,
			@RequestParam(required = false) Double currentLatitude,
			@RequestParam(required = false) Double currentLongitude) {
		return userDailyMissionService.findById(id)
				.map(mission -> UserDailyMissionResponse.from(mission, currentLatitude, currentLongitude))
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping(params = {"userId", "missionDate"})
	public List<UserDailyMissionResponse> findByUserIdAndMissionDate(
			@RequestParam Long userId,
			@RequestParam LocalDate missionDate) {
		return userDailyMissionService.findByUserIdAndMissionDate(userId, missionDate).stream()
				.map(UserDailyMissionResponse::from)
				.toList();
	}

	@PostMapping
	public ResponseEntity<UserDailyMissionResponse> create(@RequestBody CreateUserDailyMissionRequest request) {
		var saved = userDailyMissionService.create(
				request.userId(),
				request.missionId(),
				request.collectionLocationId(),
				request.missionDate()
		);
		return ResponseEntity.status(HttpStatus.CREATED).body(UserDailyMissionResponse.from(saved));
	}

	@PostMapping("/{id}/complete")
	public ResponseEntity<UserDailyMissionResponse> complete(
			@PathVariable Long id,
			@RequestBody CompleteUserDailyMissionRequest request) {
		Integer reward = request.earnedReward() != null ? request.earnedReward() : 0;
		var updated = userDailyMissionService.complete(
				id, 
				reward, 
				request.currentLatitude(), 
				request.currentLongitude()
		);
		return ResponseEntity.ok(UserDailyMissionResponse.from(updated));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteById(@PathVariable Long id) {
		if (userDailyMissionService.findById(id).isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		userDailyMissionService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
