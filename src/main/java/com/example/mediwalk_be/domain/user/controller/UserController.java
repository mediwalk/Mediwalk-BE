package com.example.mediwalk_be.domain.user.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.mediwalk_be.domain.mission.service.UserAchievementProvisioningService;
import com.example.mediwalk_be.domain.user.dto.request.CreateUserRequest;
import com.example.mediwalk_be.domain.user.dto.response.UserResponse;
import com.example.mediwalk_be.domain.user.entity.User;
import com.example.mediwalk_be.domain.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자: 프로필·누적 리워드·홈 요약")
public class UserController {

	private final UserService userService;
	private final UserAchievementProvisioningService userAchievementProvisioningService;
	private final PasswordEncoder passwordEncoder;

	@GetMapping
	@Operation(summary = "사용자 전체 조회", description = "등록된 모든 사용자 목록을 반환합니다. (관리·테스트용)")
	public List<UserResponse> findAll() {
		return userService.findAll().stream()
				.map(UserResponse::from)
				.toList();
	}

	@GetMapping("/{id}")
	@Operation(summary = "사용자 단건 조회", description = "홈 화면용: 누적 리워드·수거 횟수와 이번 달 폐의약품 수거 리워드·증가율을 포함합니다.")
	public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
		return userService.findById(id)
				.map(user -> {
				var summary = userService.getRewardMainMonthlySummary(id);
				return UserResponse.from(
						user,
						summary.thisMonthMedicineCollectionRewardTotal(),
						summary.medicineCollectionRewardIncreaseRateComparedToLastMonth()
				);
				})
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	@Operation(summary = "사용자 등록", description = "이메일 가입 사용자를 생성합니다. Google 로그인은 POST /auth/google을 사용하세요.")
	public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
		if (userService.existsByEmail(request.email())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		User user = User.builder()
				.email(request.email())
				.password(passwordEncoder.encode(request.password()))
				.name(request.name())
				.phone(request.phone())
				.birthDate(request.birthDate())
				.gender(request.gender())
				.build();
		User saved = userService.save(user);
		userAchievementProvisioningService.ensureDefaultAchievementsForUser(saved.getId());
		return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(saved));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "사용자 삭제", description = "사용자 ID 기준으로 계정을 삭제합니다. (관리·테스트용)")
	public ResponseEntity<Void> deleteById(@PathVariable Long id) {
		if (userService.findById(id).isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		userService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
