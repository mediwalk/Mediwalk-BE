package com.example.mediwalk_be.exception;

/** 요청한 리소스를 찾을 수 없을 때 던진다. {@link GlobalExceptionHandler}가 404로 변환한다. */
public class NotFoundException extends RuntimeException {

	public NotFoundException(String message) {
		super(message);
	}
}
