package com.example.mediwalk_be.exception;

/** 현재 상태와 충돌하는 요청일 때 던진다. {@link GlobalExceptionHandler}가 409로 변환한다. */
public class ConflictException extends RuntimeException {

	public ConflictException(String message) {
		super(message);
	}
}
