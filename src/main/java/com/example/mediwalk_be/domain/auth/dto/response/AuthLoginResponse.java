package com.example.mediwalk_be.domain.auth.dto.response;

import com.example.mediwalk_be.domain.user.dto.response.UserResponse;

public record AuthLoginResponse(UserResponse user) {
}
