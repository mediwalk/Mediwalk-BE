package com.example.mediwalk_be.controller;

import com.example.mediwalk_be.dto.request.CreateEventRequest;
import com.example.mediwalk_be.dto.response.EventResponse;
import com.example.mediwalk_be.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

	private final EventService eventService;

	@PostMapping
	public ResponseEntity<EventResponse> create(@RequestBody CreateEventRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(EventResponse.from(eventService.create(request)));
	}

	@GetMapping("/{id}")
	public ResponseEntity<EventResponse> findById(@PathVariable Long id) {
		return eventService.findById(id)
				.map(EventResponse::from)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping(params = "userId")
	public List<EventResponse> findByUserId(
			@RequestParam Long userId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return eventService.findByUserIdOrderByEventDateTimeDesc(userId, PageRequest.of(page, size)).stream()
				.map(EventResponse::from)
				.toList();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteById(@PathVariable Long id) {
		if (eventService.findById(id).isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		eventService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
