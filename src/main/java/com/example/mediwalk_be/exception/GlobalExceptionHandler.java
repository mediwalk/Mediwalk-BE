package com.example.mediwalk_be.exception;

import com.example.mediwalk_be.domain.common.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final String CODE_NOT_FOUND = "NOT_FOUND";
	private static final String CODE_BAD_REQUEST = "BAD_REQUEST";
	private static final String CODE_VALIDATION_ERROR = "VALIDATION_ERROR";
	private static final String CODE_INTERNAL_ERROR = "INTERNAL_ERROR";


	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
		if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("not found")) {
			return ResponseEntity
					.status(HttpStatus.NOT_FOUND)
					.body(ErrorResponse.of(CODE_NOT_FOUND, ex.getMessage(), HttpStatus.NOT_FOUND.value()));
		}
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ErrorResponse.of(CODE_BAD_REQUEST, ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
	}

	/** 요청 body/파라미터 검증 실패 (@Valid) */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
		List<ErrorResponse.FieldErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream()
				.map(fe -> new ErrorResponse.FieldErrorDetail(fe.getField(), fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid"))
				.toList();
		String message = "입력값 검증에 실패했습니다.";
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ErrorResponse.of(CODE_VALIDATION_ERROR, message, HttpStatus.BAD_REQUEST.value(), errors));
	}

	/** 그 외 예외 → 500, 클라이언트에는 상세 노출하지 않음 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception ex) {
		String message = "서버 오류가 발생했습니다.";
		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ErrorResponse.of(CODE_INTERNAL_ERROR, message, HttpStatus.INTERNAL_SERVER_ERROR.value()));
	}
}
