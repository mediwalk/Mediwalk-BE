package com.example.mediwalk_be.config.security;

import com.example.mediwalk_be.domain.user.entity.enums.UserRole;

/** Firebase 토큰 검증 후 SecurityContext에 저장되는 인증된 사용자 정보. */
public record AuthenticatedUser(Long userId, String firebaseUid, UserRole role) {
}
