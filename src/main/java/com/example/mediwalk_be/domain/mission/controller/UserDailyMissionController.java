package com.example.mediwalk_be.domain.mission.controller;

import com.example.mediwalk_be.domain.mission.dto.request.CompleteUserDailyMissionRequest;
import com.example.mediwalk_be.domain.mission.dto.request.CreateUserDailyMissionRequest;
import com.example.mediwalk_be.domain.mission.dto.response.UserDailyMissionResponse;
import com.example.mediwalk_be.domain.walk.dto.response.DestinationProximityResponse;
import com.example.mediwalk_be.domain.mission.service.UserDailyMissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/user-daily-missions")
@RequiredArgsConstructor
@Tag(name = "UserDailyMission", description = "일일 미션: 오늘의 미션 조회·완료")
public class UserDailyMissionController {

	private final UserDailyMissionService userDailyMissionService;

	@GetMapping("/{id}")
	@Operation(summary = "일일 미션 단건 조회", description = "미션 상세(제목·목적지·상태). currentLatitude/Longitude 전달 시 목적지까지 거리·도보시간을 계산합니다.")
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
	@Operation(summary = "일일 미션 수동 생성", description = "특정 날짜·미션 템플릿으로 일일 미션을 직접 생성합니다. 오늘 미션은 목록 조회 시 자동 생성됩니다.")
	public ResponseEntity<UserDailyMissionResponse> create(@RequestBody CreateUserDailyMissionRequest request) {
		var saved = userDailyMissionService.create(
				request.userId(),
				request.missionId(),
				request.collectionLocationId(),
				request.missionDate()
		);
		return ResponseEntity.status(HttpStatus.CREATED).body(UserDailyMissionResponse.from(saved));
	}

	@GetMapping("/{id}/destination-proximity")
	@Operation(
			summary = "목적지 인증 반경 확인",
			description = "현재 위치가 미션 목적지(수거함) 인증 반경(activationRadius, 기본 20m) 이내인지 확인합니다. "
					+ "이미지 인식 페이지 진입 전에 호출하세요. withinActivationRadius가 true일 때만 인증을 진행하면 됩니다."
	)
	public ResponseEntity<DestinationProximityResponse> checkDestinationProximity(
			@PathVariable Long id,
			@RequestParam double currentLatitude,
			@RequestParam double currentLongitude) {
		if (userDailyMissionService.findById(id).isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(userDailyMissionService.checkDestinationProximity(id, currentLatitude, currentLongitude));
	}

	@PostMapping("/{id}/complete")
	@Operation(summary = "일일 미션 완료", description = "미션을 COMPLETED로 변경합니다. 목적지가 있으면 현재 위치가 수거함 반경(기본 20m) 이내인지 검증합니다.")
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
	@Operation(summary = "일일 미션 삭제", description = "일일 미션 ID 기준으로 데이터를 삭제합니다. (관리·테스트용)")
	public ResponseEntity<Void> deleteById(@PathVariable Long id) {
		if (userDailyMissionService.findById(id).isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		userDailyMissionService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
