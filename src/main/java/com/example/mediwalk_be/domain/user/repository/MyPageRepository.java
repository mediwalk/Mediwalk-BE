package com.example.mediwalk_be.domain.user.repository;

import com.example.mediwalk_be.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 마이페이지에서 사용하는 회원 조회. 영속성은 {@link UserRepository}에 위임합니다.
 */
@Repository
@RequiredArgsConstructor
public class MyPageRepository {

	private final UserRepository userRepository;

	public Optional<User> findByFirebaseUid(String firebaseUid) {
		return userRepository.findByFirebaseUid(firebaseUid);
	}
}
