package com.example.mediwalk_be.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

	/**
	 * 쉼표 구분. 환경 변수 {@code CORS_ALLOWED_ORIGINS} 로 덮어쓸 수 있음.
	 */
	private String allowedOrigins =
			"https://mediwalk.site,https://www.mediwalk.site,https://api.mediwalk.site,"
					+ "http://localhost:5173,http://localhost:3000,http://127.0.0.1:5173";

	public List<String> resolvedAllowedOrigins() {
		if (allowedOrigins == null || allowedOrigins.isBlank()) {
			return List.of();
		}
		return Arrays.stream(allowedOrigins.split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.toList();
	}
}
