package com.example.mediwalk_be.domain.reward.repository;

import com.example.mediwalk_be.domain.reward.entity.Event;
import com.example.mediwalk_be.domain.reward.entity.enums.EventType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

	List<Event> findByUserIdOrderByEventDateTimeDesc(Long userId, Pageable pageable);
	List<Event> findByUserIdAndEventTypeOrderByEventDateTimeDesc(Long userId, EventType eventType, Pageable pageable);

	List<Event> findByUserIdAndEventDateTimeBetween(Long userId, LocalDateTime start, LocalDateTime end);

	long countByUserIdAndEventTypeAndEventDateTimeBetween(Long userId, EventType eventType, LocalDateTime start, LocalDateTime end);
}
