package com.example.mediwalk_be.controller;

import com.example.mediwalk_be.dto.request.CreateUserRequest;
import com.example.mediwalk_be.dto.response.UserResponse;
import com.example.mediwalk_be.entity.User;
import com.example.mediwalk_be.service.UserService;
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
				.map(UserResponse::from)
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
				.role(request.role() != null ? request.role() : com.example.mediwalk_be.entity.enums.UserRole.USER)
				.status(request.status() != null ? request.status() : com.example.mediwalk_be.entity.enums.UserStatus.ACTIVE)
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
