package com.example.mediwalk_be.config.security;

import com.example.mediwalk_be.domain.user.entity.enums.UserRole;
import com.example.mediwalk_be.exception.ForbiddenException;

/** 리소스 소유자(또는 ADMIN) 검증 유틸리티. */
public final class OwnershipGuard {

	private OwnershipGuard() {
	}

	public static void requireOwner(AuthenticatedUser currentUser, Long resourceOwnerId) {
		if (!currentUser.userId().equals(resourceOwnerId)) {
			throw new ForbiddenException("본인 소유의 리소스만 접근할 수 있습니다.");
		}
	}

	public static void requireOwnerOrAdmin(AuthenticatedUser currentUser, Long resourceOwnerId) {
		if (currentUser.role() == UserRole.ADMIN) {
			return;
		}
		requireOwner(currentUser, resourceOwnerId);
	}
}
