
package com.cakedelight.order.exception;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	/**
	 * Handles exceptions that contain a specific HTTP status and reason.
	 */
	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ApiErrorResponse> handleResponseStatusException(ResponseStatusException exception) {

		HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());

		logger.warn("Request failed with status {}: {}", status.value(), exception.getReason());

		ApiErrorResponse response = new ApiErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(),
				exception.getReason());

		return new ResponseEntity<>(response, status);
	}

	/**
	 * Handles validation errors raised when request data fails validation.
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {

		String message = "Invalid request data";

		if (exception.getBindingResult().getFieldError() != null) {
			message = exception.getBindingResult().getFieldError().getDefaultMessage();
		}

		logger.warn("Request validation failed: {}", message);

		ApiErrorResponse response = new ApiErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST.getReasonPhrase(), message);

		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Handles unexpected exceptions and returns a generic server error response.
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleGeneralException(Exception exception) {

		logger.error("Unexpected error occurred", exception);

		ApiErrorResponse response = new ApiErrorResponse(LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(),
				HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "An unexpected error occurred");

		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
