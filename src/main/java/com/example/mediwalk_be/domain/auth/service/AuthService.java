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
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@ConditionalOnBean(FirebaseApp.class)
public class AuthService {

	private static final String FIREBASE_PASSWORD_PLACEHOLDER = "{FIREBASE_OAUTH}";

	private final UserService userService;

	public AuthLoginResponse loginWithGoogleIdToken(String idToken) {
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
						.name(displayName.trim())
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
}
