package com.example.mediwalk_be.config.security;

import com.example.mediwalk_be.domain.auth.service.FirebaseIdTokenService;
import com.example.mediwalk_be.domain.user.entity.User;
import com.example.mediwalk_be.domain.user.entity.enums.UserRole;
import com.example.mediwalk_be.domain.user.service.UserService;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Authorization: Bearer <Firebase ID 토큰> 헤더를 검증해 SecurityContext에 인증 정보를 채운다.
 * 토큰이 없거나 검증/사용자 조회에 실패하면 익명 상태로 다음 필터로 넘기고,
 * 보호된 엔드포인트에 대한 401/403 응답은 SecurityConfig의 entry point/handler가 처리한다.
 */
@Component
@RequiredArgsConstructor
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";

	private final FirebaseIdTokenService firebaseIdTokenService;
	private final UserService userService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header != null && header.startsWith(BEARER_PREFIX)) {
			authenticate(header.substring(BEARER_PREFIX.length()).trim());
		}
		filterChain.doFilter(request, response);
	}

	private void authenticate(String idToken) {
		try {
			FirebaseToken token = firebaseIdTokenService.verify(idToken);
			userService.findByFirebaseUid(token.getUid()).ifPresent(this::setAuthentication);
		} catch (Exception e) {
			// 토큰 무효/만료, Firebase 미설정 등 -> 익명 상태 유지
		}
	}

	private void setAuthentication(User user) {
		AuthenticatedUser principal = new AuthenticatedUser(user.getId(), user.getFirebaseUid(), user.getRole());
		List<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
		if (user.getRole() == UserRole.ADMIN) {
			authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
		}
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(principal, null, authorities));
	}
}
