package com.example.mediwalk_be.domain.auth.service;

import com.example.mediwalk_be.domain.auth.dto.response.AuthLoginResponse;
import com.example.mediwalk_be.domain.user.dto.response.UserResponse;
import com.example.mediwalk_be.domain.user.entity.User;
import com.example.mediwalk_be.domain.user.entity.enums.UserRole;
import com.example.mediwalk_be.domain.user.entity.enums.UserStatus;
import com.example.mediwalk_be.domain.user.service.UserService;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

	private static final String FIREBASE_PASSWORD_PLACEHOLDER = "{FIREBASE_OAUTH}";
	private static final int MAX_NAME_LENGTH = 50;

	private final FirebaseIdTokenService firebaseIdTokenService;
	private final UserService userService;

	public AuthLoginResponse loginWithGoogleIdToken(String idToken) {
		FirebaseToken token = firebaseIdTokenService.verify(idToken);
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

		String pictureUrl = blankToNull(token.getPicture());

		Optional<User> byUid = userService.findByFirebaseUid(uid);
		if (byUid.isPresent()) {
			User u = byUid.get();
			if (pictureUrl != null) {
				u.updateProfileImageUrl(pictureUrl);
				u = userService.save(u);
			}
			return new AuthLoginResponse(UserResponse.from(u));
		}

		User user = userService.findByEmail(normalizedEmail)
				.map(u -> {
					u.assignFirebaseUid(uid);
					if (pictureUrl != null) {
						u.updateProfileImageUrl(pictureUrl);
					}
					return userService.save(u);
				})
				.orElseGet(() -> userService.save(User.builder()
						.email(normalizedEmail)
						.password(FIREBASE_PASSWORD_PLACEHOLDER)
						.name(nameForDb)
						.role(UserRole.USER)
						.status(UserStatus.ACTIVE)
						.firebaseUid(uid)
						.profileImageUrl(pictureUrl)
						.build()));

		return new AuthLoginResponse(UserResponse.from(user));
	}

	private static String blankToNull(String s) {
		if (s == null || s.isBlank()) {
			return null;
		}
		return s.trim();
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
