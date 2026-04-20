package com.example.mediwalk_be.domain.walk.client.dto;

import java.util.List;

public record AiVerificationRequestPayload(
		List<Instance> instances
) {
	public record Instance(String content) {}

	public static AiVerificationRequestPayload of(String base64Image) {
		return new AiVerificationRequestPayload(List.of(new Instance(base64Image)));
	}
}
