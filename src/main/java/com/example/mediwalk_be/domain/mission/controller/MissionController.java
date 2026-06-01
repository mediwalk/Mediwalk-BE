package com.example.mediwalk_be.domain.mission.controller;

import com.example.mediwalk_be.domain.mission.dto.request.CreateMissionRequest;
import com.example.mediwalk_be.domain.mission.dto.response.MissionResponse;
import com.example.mediwalk_be.domain.mission.entity.Mission;
import com.example.mediwalk_be.domain.mission.service.MissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
@Tag(name = "Mission", description = "미션 마스터: 템플릿 조회·시드 (폐의약품 수거·운동)")
public class MissionController {

	private final MissionService missionService;

	@GetMapping
	@Operation(summary = "미션 템플릿 전체 조회")
	public List<MissionResponse> findAll() {
		return missionService.findAll().stream()
				.map(MissionResponse::from)
				.toList();
	}

	@GetMapping("/{id}")
	@Operation(summary = "미션 템플릿 단건 조회")
	public ResponseEntity<MissionResponse> findById(@PathVariable Long id) {
		return missionService.findById(id)
				.map(MissionResponse::from)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping(params = "missionType")
	@Operation(summary = "미션 타입별 조회", description = "WASTE_MEDICINE_COLLECTION 또는 EXERCISE 타입으로 필터링합니다.")
	public List<MissionResponse> findByMissionType(@RequestParam com.example.mediwalk_be.domain.mission.entity.enums.MissionType missionType) {
		return missionService.findByMissionType(missionType).stream()
				.map(MissionResponse::from)
				.toList();
	}

	@PostMapping
	@Operation(summary = "미션 템플릿 등록", description = "새 미션 마스터를 생성합니다. (시드·관리용)")
	public ResponseEntity<MissionResponse> create(@RequestBody CreateMissionRequest request) {
		Mission entity = Mission.builder()
				.missionType(request.missionType())
				.title(request.title())
				.description(request.description())
				.baseRewardAmount(request.baseRewardAmount() != null ? request.baseRewardAmount() : 3000)
				.build();
		Mission saved = missionService.save(entity);
		return ResponseEntity.status(HttpStatus.CREATED).body(MissionResponse.from(saved));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "미션 템플릿 삭제", description = "미션 ID 기준으로 템플릿을 삭제합니다. (관리·테스트용)")
	public ResponseEntity<Void> deleteById(@PathVariable Long id) {
		if (missionService.findById(id).isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		missionService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
