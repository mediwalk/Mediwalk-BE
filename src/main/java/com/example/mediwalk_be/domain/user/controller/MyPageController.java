package com.example.mediwalk_be.domain.user.controller;

import com.example.mediwalk_be.domain.user.dto.response.MyPageProfileResponse;
import com.example.mediwalk_be.domain.user.service.MyPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
@Tag(name = "MyPage", description = "마이페이지 (프로필 카드)")
public class MyPageController {

	private final MyPageService myPageService;

	@GetMapping("/profile")
	@Operation(
			summary = "마이페이지 프로필",
			description = "프로필 카드용 이름·이메일·이미지 URL. 헤더: Authorization: Bearer {Firebase ID 토큰}"
	)
	public MyPageProfileResponse getProfile(
			@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
	) {
		return myPageService.getProfile(requireBearerToken(authorization));
	}

	private static String requireBearerToken(String authorization) {
		if (authorization == null || !authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization: Bearer {Firebase ID 토큰} 이 필요합니다.");
		}
		String token = authorization.substring(7).trim();
		if (token.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization: Bearer {Firebase ID 토큰} 이 필요합니다.");
		}
		return token;
	}
}
