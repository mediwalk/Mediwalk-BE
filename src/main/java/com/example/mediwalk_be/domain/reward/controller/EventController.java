package com.example.mediwalk_be.domain.reward.controller;

import com.example.mediwalk_be.domain.reward.dto.request.CreateEventRequest;
import com.example.mediwalk_be.domain.reward.dto.response.EventResponse;
import com.example.mediwalk_be.domain.reward.entity.enums.EventType;
import com.example.mediwalk_be.domain.reward.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Reward", description = "리워드 이벤트(수거/미션) 조회 및 생성 API")
public class EventController {

	private final EventService eventService;

	@PostMapping
	@Operation(summary = "이벤트 생성", description = "수거/운동 미션 등 이벤트를 생성하고, 리워드가 있으면 적립 거래를 함께 기록합니다.")
	public ResponseEntity<EventResponse> create(@RequestBody CreateEventRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(EventResponse.from(eventService.create(request)));
	}

	@GetMapping("/{id}")
	@Operation(summary = "이벤트 단건 조회", description = "이벤트 ID로 상세 정보를 조회합니다.")
	public ResponseEntity<EventResponse> findById(@PathVariable Long id) {
		return eventService.findById(id)
				.map(EventResponse::from)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping(params = "userId")
	@Operation(summary = "이벤트 목록 조회", description = "사용자 이벤트 목록을 최신순으로 조회합니다. eventType 전달 시 해당 타입만 필터링됩니다.")
	public List<EventResponse> findByUserId(
			@RequestParam Long userId,
			@RequestParam(required = false) EventType eventType,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return eventService.findByUserIdWithOptionalEventType(userId, eventType, PageRequest.of(page, size)).stream()
				.map(EventResponse::from)
				.toList();
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "이벤트 삭제", description = "이벤트 ID 기준으로 데이터를 삭제합니다.")
	public ResponseEntity<Void> deleteById(@PathVariable Long id) {
		if (eventService.findById(id).isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		eventService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
