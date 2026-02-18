package com.example.mediwalk_be.service;

import com.example.mediwalk_be.dto.request.CreateEventRequest;
import com.example.mediwalk_be.entity.*;
import com.example.mediwalk_be.entity.enums.EventType;
import com.example.mediwalk_be.entity.enums.RewardTransactionType;
import com.example.mediwalk_be.repository.CollectionLocationRepository;
import com.example.mediwalk_be.repository.EventRepository;
import com.example.mediwalk_be.repository.RouteRepository;
import com.example.mediwalk_be.repository.RewardTransactionRepository;
import com.example.mediwalk_be.repository.UserRepository;
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
	private final UserRepository userRepository;
	private final CollectionLocationRepository collectionLocationRepository;
	private final RouteRepository routeRepository;
	private final RewardTransactionRepository rewardTransactionRepository;

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
	public Event create(CreateEventRequest request) {
		User user = userRepository.findById(request.userId())
				.orElseThrow(() -> new IllegalArgumentException("User not found: id=" + request.userId()));
		CollectionLocation collectionLocation = request.collectionLocationId() != null
				? collectionLocationRepository.findById(request.collectionLocationId()).orElse(null)
				: null;
		Route route = request.routeId() != null
				? routeRepository.findById(request.routeId()).orElse(null)
				: null;

		int rewardAmount = request.rewardAmount() != null ? request.rewardAmount() : 0;

		Event event = Event.builder()
				.user(user)
				.eventType(request.eventType())
				.title(request.title() != null ? request.title() : request.eventType().name())
				.rewardAmount(rewardAmount)
				.eventDateTime(request.eventDateTime() != null ? request.eventDateTime() : LocalDateTime.now())
				.locationName(request.locationName())
				.collectionLocation(collectionLocation)
				.imageUrl(request.imageUrl())
				.route(route)
				.build();
		event = eventRepository.save(event);

		if (request.eventType() == EventType.MEDICINE_COLLECTION) {
			user.incrementTotalCollectionsCount();
		}
		if (rewardAmount > 0) {
			user.addAccumulatedReward(rewardAmount);
			RewardTransaction accumulation = RewardTransaction.builder()
					.user(user)
					.event(event)
					.amount(rewardAmount)
					.transactionType(RewardTransactionType.ACCUMULATION)
					.transactionDate(event.getEventDateTime())
					.description(event.getTitle())
					.build();
			rewardTransactionRepository.save(accumulation);
		}
		return event;
	}

	@Transactional
	public void deleteById(Long id) {
		eventRepository.deleteById(id);
	}
}
