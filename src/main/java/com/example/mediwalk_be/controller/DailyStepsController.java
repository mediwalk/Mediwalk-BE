package com.example.mediwalk_be.controller;

import com.example.mediwalk_be.dto.request.AddDailyStepsRequest;
import com.example.mediwalk_be.dto.response.DailyStepsResponse;
import com.example.mediwalk_be.service.DailyStepsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api/daily-steps")
@RequiredArgsConstructor
public class DailyStepsController {

	private final DailyStepsService dailyStepsService;

	@GetMapping("/{id}")
	public ResponseEntity<DailyStepsResponse> findById(@PathVariable Long id) {
		return dailyStepsService.findById(id)
				.map(DailyStepsResponse::from)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping(params = {"userId", "date"})
	public ResponseEntity<DailyStepsResponse> findByUserIdAndDate(
			@RequestParam Long userId,
			@RequestParam LocalDate date) {
		Optional<DailyStepsResponse> result = dailyStepsService.findByUserIdAndDate(userId, date)
				.map(DailyStepsResponse::from);
		return result.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@PostMapping("/get-or-create")
	public ResponseEntity<DailyStepsResponse> getOrCreate(
			@RequestParam Long userId,
			@RequestParam LocalDate date) {
		var ds = dailyStepsService.getOrCreate(userId, date);
		return ResponseEntity.status(HttpStatus.CREATED).body(DailyStepsResponse.from(ds));
	}

	@PostMapping("/{id}/add-steps")
	public ResponseEntity<DailyStepsResponse> addSteps(
			@PathVariable Long id,
			@RequestBody AddDailyStepsRequest request) {
		var updated = dailyStepsService.addSteps(id, request.count());
		return ResponseEntity.ok(DailyStepsResponse.from(updated));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteById(@PathVariable Long id) {
		if (dailyStepsService.findById(id).isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		dailyStepsService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
