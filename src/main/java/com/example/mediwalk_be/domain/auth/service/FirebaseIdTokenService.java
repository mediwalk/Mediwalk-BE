package com.example.mediwalk_be.domain.auth.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class FirebaseIdTokenService {

	private final ObjectProvider<FirebaseApp> firebaseAppProvider;

	public FirebaseToken verify(String idToken) {
		if (firebaseAppProvider.getIfAvailable() == null) {
			throw new ResponseStatusException(
					HttpStatus.SERVICE_UNAVAILABLE,
					"Firebase가 설정되지 않았습니다. firebase.enabled=true 와 서비스 계정 credentials 를 설정하세요."
			);
		}
		try {
			return FirebaseAuth.getInstance().verifyIdToken(idToken);
		} catch (FirebaseAuthException e) {
			throw new IllegalArgumentException("Invalid or expired Firebase ID token");
		}
	}
}
