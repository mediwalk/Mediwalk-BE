package com.example.mediwalk_be.config.security;

import com.example.mediwalk_be.domain.common.dto.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;

/** 인증/인가 실패 시 {@link ErrorResponse} 형태의 JSON을 직접 작성한다. */
final class SecurityResponseWriter {

	private SecurityResponseWriter() {
	}

	static void write(HttpServletResponse response, ObjectMapper objectMapper, HttpStatus status, String code, String message)
			throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		objectMapper.writeValue(response.getWriter(), ErrorResponse.of(code, message, status.value()));
	}
}
