package com.example.mediwalk_be.exception;

/** 본인 소유가 아닌 리소스에 접근할 때 던진다. {@link GlobalExceptionHandler}가 403으로 변환한다. */
public class ForbiddenException extends RuntimeException {

	public ForbiddenException(String message) {
		super(message);
	}
}
