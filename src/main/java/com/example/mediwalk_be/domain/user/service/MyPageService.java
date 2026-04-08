package com.example.mediwalk_be.domain.user.service;

import com.example.mediwalk_be.domain.auth.service.FirebaseIdTokenService;
import com.example.mediwalk_be.domain.user.dto.response.MyPageProfileResponse;
import com.example.mediwalk_be.domain.user.entity.User;
import com.example.mediwalk_be.domain.user.repository.MyPageRepository;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

	private final FirebaseIdTokenService firebaseIdTokenService;
	private final MyPageRepository myPageRepository;

	public MyPageProfileResponse getProfile(String idToken) {
		FirebaseToken token = firebaseIdTokenService.verify(idToken);
		User user = myPageRepository.findByFirebaseUid(token.getUid())
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.UNAUTHORIZED,
						"등록된 회원이 아닙니다. Google 로그인 API를 먼저 호출해 주세요."
				));
		String picture = firstNonBlank(blankToNull(token.getPicture()), user.getProfileImageUrl());
		return MyPageProfileResponse.from(user, picture);
	}

	private static String blankToNull(String s) {
		if (s == null || s.isBlank()) {
			return null;
		}
		return s.trim();
	}

	private static String firstNonBlank(String a, String b) {
		if (a != null && !a.isBlank()) {
			return a.trim();
		}
		if (b != null && !b.isBlank()) {
			return b.trim();
		}
		return null;
	}
}
