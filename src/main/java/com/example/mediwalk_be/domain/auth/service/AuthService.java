package com.example.mediwalk_be.domain.auth.service;

import com.example.mediwalk_be.domain.auth.dto.response.AuthLoginResponse;
import com.example.mediwalk_be.domain.user.dto.response.UserResponse;
import com.example.mediwalk_be.domain.user.entity.User;
import com.example.mediwalk_be.domain.user.entity.enums.UserRole;
import com.example.mediwalk_be.domain.user.entity.enums.UserStatus;
import com.example.mediwalk_be.domain.user.service.UserService;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

	private static final String FIREBASE_PASSWORD_PLACEHOLDER = "{FIREBASE_OAUTH}";
	private static final int MAX_NAME_LENGTH = 50;

	private final ObjectProvider<FirebaseApp> firebaseAppProvider;
	private final UserService userService;

	public AuthLoginResponse loginWithGoogleIdToken(String idToken) {
		if (firebaseAppProvider.getIfAvailable() == null) {
			throw new ResponseStatusException(
					HttpStatus.SERVICE_UNAVAILABLE,
					"Firebase가 설정되지 않았습니다. firebase.enabled=true 와 서비스 계정 credentials 를 설정하세요."
			);
		}
		FirebaseToken token = verifyIdToken(idToken);
		String uid = token.getUid();
		String email = token.getEmail();
		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("Firebase token has no email claim");
		}
		String normalizedEmail = email.trim().toLowerCase();
		String rawName = token.getName();
		final String displayName;
		if (rawName == null || rawName.isBlank()) {
			int at = normalizedEmail.indexOf('@');
			displayName = at > 0 ? normalizedEmail.substring(0, at) : normalizedEmail;
		} else {
			displayName = rawName.trim();
		}
		String nameForDb = truncateToMaxNameLength(displayName);

		Optional<User> byUid = userService.findByFirebaseUid(uid);
		if (byUid.isPresent()) {
			return new AuthLoginResponse(UserResponse.from(byUid.get()));
		}

		User user = userService.findByEmail(normalizedEmail)
				.map(u -> {
					u.assignFirebaseUid(uid);
					return userService.save(u);
				})
				.orElseGet(() -> userService.save(User.builder()
						.email(normalizedEmail)
						.password(FIREBASE_PASSWORD_PLACEHOLDER)
						.name(nameForDb)
						.role(UserRole.USER)
						.status(UserStatus.ACTIVE)
						.firebaseUid(uid)
						.build()));

		return new AuthLoginResponse(UserResponse.from(user));
	}

	private FirebaseToken verifyIdToken(String idToken) {
		try {
			return FirebaseAuth.getInstance().verifyIdToken(idToken);
		} catch (FirebaseAuthException e) {
			throw new IllegalArgumentException("Invalid or expired Firebase ID token");
		}
	}

	private static String truncateToMaxNameLength(String name) {
		if (name == null || name.isEmpty()) {
			return "?";
		}
		String t = name.trim();
		if (t.length() <= MAX_NAME_LENGTH) {
			return t;
		}
		return t.substring(0, MAX_NAME_LENGTH);
	}
}
