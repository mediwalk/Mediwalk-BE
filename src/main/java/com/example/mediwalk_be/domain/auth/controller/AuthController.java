package com.example.mediwalk_be.domain.auth.controller;

import com.example.mediwalk_be.domain.auth.dto.request.GoogleLoginRequest;
import com.example.mediwalk_be.domain.auth.dto.response.AuthLoginResponse;
import com.example.mediwalk_be.domain.auth.service.AuthService;
import com.google.firebase.FirebaseApp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@ConditionalOnBean(FirebaseApp.class)
@Tag(name = "Auth", description = "Firebase Google 로그인 (온보딩 화면 연동)")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/google")
	@Operation(
			summary = "Google 로그인",
			description = "클라이언트에서 Firebase로 받은 ID 토큰을 검증하고, 사용자를 생성하거나 기존 계정에 Firebase UID를 연결합니다."
	)
	public ResponseEntity<AuthLoginResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
		return ResponseEntity.ok(authService.loginWithGoogleIdToken(request.idToken()));
	}
}
