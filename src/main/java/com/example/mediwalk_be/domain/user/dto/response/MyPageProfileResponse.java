package com.example.mediwalk_be.domain.user.dto.response;

import com.example.mediwalk_be.domain.user.entity.User;

public record MyPageProfileResponse(
		String name,
		String email,
		String profileImageUrl
) {
	public static MyPageProfileResponse from(User user, String resolvedProfileImageUrl) {
		return new MyPageProfileResponse(
				user.getName(),
				user.getEmail(),
				resolvedProfileImageUrl
		);
	}
}
