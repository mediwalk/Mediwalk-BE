package com.example.mediwalk_be.domain.walk.controller;

import com.example.mediwalk_be.domain.walk.dto.request.AddDailyStepsRequest;
import com.example.mediwalk_be.domain.walk.dto.response.DailyStepsResponse;
import com.example.mediwalk_be.domain.walk.service.DailyStepsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api/daily-steps")
@RequiredArgsConstructor
@Tag(name = "DailySteps", description = "걸음 수: 일별 걸음 수 조회 및 누적")
public class DailyStepsController {

	private final DailyStepsService dailyStepsService;

	@GetMapping("/{id}")
	@Operation(summary = "걸음 수 단건 조회")
	public ResponseEntity<DailyStepsResponse> findById(@PathVariable Long id) {
		return dailyStepsService.findById(id)
				.map(DailyStepsResponse::from)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping(params = {"userId", "date"})
	@Operation(summary = "날짜별 걸음 수 조회", description = "userId와 date(yyyy-MM-dd)로 해당 날짜의 걸음 수를 조회합니다.")
	public ResponseEntity<DailyStepsResponse> findByUserIdAndDate(
			@RequestParam Long userId,
			@RequestParam LocalDate date) {
		Optional<DailyStepsResponse> result = dailyStepsService.findByUserIdAndDate(userId, date)
				.map(DailyStepsResponse::from);
		return result.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@PostMapping("/get-or-create")
	@Operation(summary = "걸음 수 조회 또는 생성", description = "해당 날짜 걸음 수 레코드가 없으면 0으로 생성 후 반환합니다.")
	public ResponseEntity<DailyStepsResponse> getOrCreate(
			@RequestParam Long userId,
			@RequestParam LocalDate date) {
		var ds = dailyStepsService.getOrCreate(userId, date);
		return ResponseEntity.status(HttpStatus.CREATED).body(DailyStepsResponse.from(ds));
	}

	@PostMapping("/{id}/add-steps")
	@Operation(summary = "걸음 수 누적", description = "걸음 수를 누적합니다. 거리·칼로리는 자동 계산됩니다. count는 1~20000 사이여야 합니다.")
	public ResponseEntity<DailyStepsResponse> addSteps(
			@PathVariable Long id,
			@Valid @RequestBody AddDailyStepsRequest request) {
		var updated = dailyStepsService.addSteps(id, request.count());
		return ResponseEntity.ok(DailyStepsResponse.from(updated));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "걸음 수 레코드 삭제")
	public ResponseEntity<Void> deleteById(@PathVariable Long id) {
		if (dailyStepsService.findById(id).isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		dailyStepsService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
