package com.example.mediwalk_be.service;

import com.example.mediwalk_be.entity.Event;
import com.example.mediwalk_be.entity.enums.EventType;
import com.example.mediwalk_be.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

	private final EventRepository eventRepository;

	public Optional<Event> findById(Long id) {
		return eventRepository.findById(id);
	}

	public Event getById(Long id) {
		return eventRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Event not found: id=" + id));
	}

	public List<Event> findByUserIdOrderByEventDateTimeDesc(Long userId, Pageable pageable) {
		return eventRepository.findByUserIdOrderByEventDateTimeDesc(userId, pageable);
	}

	public List<Event> findByUserIdAndEventDateTimeBetween(Long userId, LocalDateTime start, LocalDateTime end) {
		return eventRepository.findByUserIdAndEventDateTimeBetween(userId, start, end);
	}

	public long countByUserIdAndEventTypeAndEventDateTimeBetween(Long userId, EventType eventType, LocalDateTime start, LocalDateTime end) {
		return eventRepository.countByUserIdAndEventTypeAndEventDateTimeBetween(userId, eventType, start, end);
	}

	@Transactional
	public Event save(Event event) {
		return eventRepository.save(event);
	}

	@Transactional
	public void deleteById(Long id) {
		eventRepository.deleteById(id);
	}
}
