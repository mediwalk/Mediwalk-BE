package com.example.mediwalk_be.domain.mission.controller;

import com.example.mediwalk_be.config.security.AuthenticatedUser;
import com.example.mediwalk_be.domain.mission.dto.request.AddUserAchievementProgressRequest;
import com.example.mediwalk_be.domain.mission.dto.response.UserAchievementResponse;
import com.example.mediwalk_be.domain.mission.service.UserAchievementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-achievements")
@RequiredArgsConstructor
@Tag(name = "UserAchievement", description = "사용자 업적: 진행도 조회·관리")
public class UserAchievementController {

	private final UserAchievementService userAchievementService;

	@GetMapping("/{id}")
	@Operation(summary = "사용자 업적 단건 조회")
	public ResponseEntity<UserAchievementResponse> findById(@PathVariable Long id) {
		return userAchievementService.findById(id)
				.map(UserAchievementResponse::from)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping
	@Operation(summary = "사용자 업적 목록 조회", description = "현재 사용자의 모든 업적 진행도를 반환합니다. 앱에서는 reward-main API 사용을 권장합니다.")
	public List<UserAchievementResponse> findByUserId(@AuthenticationPrincipal AuthenticatedUser currentUser) {
		return userAchievementService.findByUserId(currentUser.userId()).stream()
				.map(UserAchievementResponse::from)
				.toList();
	}

	@PostMapping("/get-or-create")
	@Operation(summary = "사용자 업적 조회 또는 생성", description = "achievementId에 대한 현재 사용자의 진행 레코드가 없으면 생성합니다.")
	public ResponseEntity<UserAchievementResponse> getOrCreate(
			@AuthenticationPrincipal AuthenticatedUser currentUser,
			@RequestParam Long achievementId) {
		var ua = userAchievementService.getOrCreate(currentUser.userId(), achievementId);
		return ResponseEntity.status(HttpStatus.CREATED).body(UserAchievementResponse.from(ua));
	}

	@PostMapping("/{id}/add-progress")
	@Operation(summary = "업적 진행도 추가", description = "진행도를 delta만큼 증가시킵니다. (테스트용 — 실서비스는 이벤트·걸음 수로 자동 sync)")
	public ResponseEntity<UserAchievementResponse> addProgress(
			@PathVariable Long id,
			@RequestBody AddUserAchievementProgressRequest request) {
		var updated = userAchievementService.addProgress(id, request.delta());
		return ResponseEntity.ok(UserAchievementResponse.from(updated));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "사용자 업적 삭제", description = "사용자 업적 ID 기준으로 진행 레코드를 삭제합니다. (관리·테스트용)")
	public ResponseEntity<Void> deleteById(@PathVariable Long id) {
		if (userAchievementService.findById(id).isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		userAchievementService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
