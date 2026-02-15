package com.example.mediwalk_be.controller;

import com.example.mediwalk_be.dto.request.CreateMissionRequest;
import com.example.mediwalk_be.dto.response.MissionResponse;
import com.example.mediwalk_be.entity.Mission;
import com.example.mediwalk_be.service.MissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
public class MissionController {

	private final MissionService missionService;

	@GetMapping
	public List<MissionResponse> findAll() {
		return missionService.findAll().stream()
				.map(MissionResponse::from)
				.toList();
	}

	@GetMapping("/{id}")
	public ResponseEntity<MissionResponse> findById(@PathVariable Long id) {
		return missionService.findById(id)
				.map(MissionResponse::from)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping(params = "missionType")
	public List<MissionResponse> findByMissionType(@RequestParam com.example.mediwalk_be.entity.enums.MissionType missionType) {
		return missionService.findByMissionType(missionType).stream()
				.map(MissionResponse::from)
				.toList();
	}

	@PostMapping
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
	public ResponseEntity<Void> deleteById(@PathVariable Long id) {
		if (missionService.findById(id).isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		missionService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
