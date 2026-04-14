package com.example.mediwalk_be.domain.mission.controller;

import com.example.mediwalk_be.domain.mission.dto.request.CompleteUserDailyMissionRequest;
import com.example.mediwalk_be.domain.mission.dto.request.CreateUserDailyMissionRequest;
import com.example.mediwalk_be.domain.mission.dto.response.UserDailyMissionResponse;
import com.example.mediwalk_be.domain.mission.service.UserDailyMissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

	@Operation(summary = "일일 미션 목록", description = "missionDate가 오늘이면 폐의약품 수거·운동 미션이 없을 경우 자동 생성 후 반환합니다. 선택적으로 현재 위치를 전달하면 목적지까지 거리/도보시간을 계산합니다.")
	@GetMapping(params = {"userId", "missionDate"})
	public List<UserDailyMissionResponse> findByUserIdAndMissionDate(
			@RequestParam Long userId,
			@RequestParam LocalDate missionDate,
			@Parameter(description = "현재 위도 (선택). 목록 항목의 distanceMeters 등 계산에 사용")
			@RequestParam(required = false) Double currentLatitude,
			@Parameter(description = "현재 경도 (선택). 목록 항목의 distanceMeters 등 계산에 사용")
			@RequestParam(required = false) Double currentLongitude) {
		if (missionDate.equals(LocalDate.now())) {
			userDailyMissionService.ensureTodayMissions(userId, missionDate, currentLatitude, currentLongitude);
		}
		return userDailyMissionService.findByUserIdAndMissionDate(userId, missionDate).stream()
				.map(udm -> UserDailyMissionResponse.from(udm, currentLatitude, currentLongitude))
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
