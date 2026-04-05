package com.example.mediwalk_be.domain.user.controller;

import com.example.mediwalk_be.domain.user.dto.request.CreateUserRequest;
import com.example.mediwalk_be.domain.user.dto.response.UserResponse;
import com.example.mediwalk_be.domain.user.entity.User;
import com.example.mediwalk_be.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping
	public List<UserResponse> findAll() {
		return userService.findAll().stream()
				.map(UserResponse::from)
				.toList();
	}

	@GetMapping("/{id}")
	public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
		return userService.findById(id)
				.map(user -> {
					var summary = userService.getRewardSummaryForHome(id);
					return UserResponse.from(
							user,
							summary.lastMonthRewardTotal(),
							summary.thisMonthRewardTotal(),
							summary.rewardIncreaseRateComparedToLastMonth()
					);
				})
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<UserResponse> create(@RequestBody CreateUserRequest request) {
		if (userService.existsByEmail(request.email())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		User user = User.builder()
				.email(request.email())
				.password(request.password())
				.name(request.name())
				.phone(request.phone())
				.birthDate(request.birthDate())
				.gender(request.gender())
				.role(request.role() != null ? request.role() : com.example.mediwalk_be.domain.user.entity.enums.UserRole.USER)
				.status(request.status() != null ? request.status() : com.example.mediwalk_be.domain.user.entity.enums.UserStatus.ACTIVE)
				.build();
		User saved = userService.save(user);
		return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(saved));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteById(@PathVariable Long id) {
		if (userService.findById(id).isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		userService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
