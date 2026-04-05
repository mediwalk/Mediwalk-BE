package com.example.mediwalk_be.domain.common.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
	String code,
	String message,
	int status,
	List<FieldErrorDetail> errors
) {
	public static ErrorResponse of(String code, String message, int status) {
		return new ErrorResponse(code, message, status, null);
	}

	public static ErrorResponse of(String code, String message, int status, List<FieldErrorDetail> errors) {
		return new ErrorResponse(code, message, status, errors);
	}

	public record FieldErrorDetail(String field, String reason) {}
}
