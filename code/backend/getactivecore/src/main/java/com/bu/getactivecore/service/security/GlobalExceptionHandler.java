package com.bu.getactivecore.service.security;

import static com.bu.getactivecore.shared.ErrorCode.DATA_STRUCTURE_INVALID;
import static com.bu.getactivecore.shared.ErrorCode.UNSUPPORTED_MEDIA_TYPE;
import static com.bu.getactivecore.shared.ErrorCode.UNSUPPORTED_OPERATION;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.bu.getactivecore.shared.ApiErrorPayload;
import com.bu.getactivecore.shared.ApiErrorResponse;
import com.bu.getactivecore.shared.exception.ApiException;

/**
 * Global exception handler to standardize API error responses across the
 * application.
 *
 * <p>
 * Extends {@link ResponseEntityExceptionHandler} to customize handling of
 * specific exceptions and provide consistent error payloads wrapped in
 * {@link ApiErrorResponse}.
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	/**
	 * Builds a {@link ResponseEntity} containing the given {@link ApiErrorPayload}.
	 *
	 * @param apiErrorPayload The error details to include in the response.
	 * @return A {@link ResponseEntity} containing the error response.
	 */
	private ResponseEntity<Object> buildResponseEntity(ApiErrorPayload apiErrorPayload) {
		return new ResponseEntity<>(new ApiErrorResponse(apiErrorPayload), apiErrorPayload.getStatus());
	}

	/**
	 * Handles {@link HttpMessageNotReadableException} by returning a structured
	 * error response with a predefined error code and message.
	 *
	 * @param ex      The exception that occurred.
	 * @param headers The HTTP headers of the request.
	 * @param status  The HTTP status code.
	 * @param request The web request that caused the exception.
	 * @return A {@link ResponseEntity} containing the error response.
	 */
	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		return buildResponseEntity(ApiErrorPayload.builder().errorCode(DATA_STRUCTURE_INVALID).status(BAD_REQUEST)
				.message(DATA_STRUCTURE_INVALID.getDetails()).debugMessage(ex.getLocalizedMessage()).build());
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		Map<String, List<String>> validationErrors = new LinkedHashMap<>();

		// Field-level validation errors
		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			validationErrors.computeIfAbsent(error.getField(), key -> new ArrayList<>()).add(error.getDefaultMessage());
		}

		// Global-level validation errors
		for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
			validationErrors.computeIfAbsent(error.getObjectName(), key -> new ArrayList<>())
					.add(error.getDefaultMessage());
		}
		return buildResponseEntity(ApiErrorPayload.builder().errorCode(DATA_STRUCTURE_INVALID).status(BAD_REQUEST)
				.message(DATA_STRUCTURE_INVALID.getDetails()).validationErrors(validationErrors)
				.debugMessage(ex.getLocalizedMessage()).build());
	}

	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		String errorMessage = String.format("Supported content type(s) %s but received '%s'",
				ex.getSupportedMediaTypes(), ex.getContentType());
		return buildResponseEntity(
				ApiErrorPayload.builder().errorCode(UNSUPPORTED_MEDIA_TYPE).status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
						.message(errorMessage).debugMessage(ex.getLocalizedMessage()).build());
	}

	@Override
	protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		String errorMessage = String.format("Supported HTTP method(s) %s but received %s", ex.getSupportedHttpMethods(),
				ex.getMethod());
		return buildResponseEntity(ApiErrorPayload.builder().errorCode(UNSUPPORTED_OPERATION).status(METHOD_NOT_ALLOWED)
				.message(errorMessage).debugMessage(ex.getLocalizedMessage()).build());
	}

	/**
	 * Handles custom {@link ApiException} by returning a structured error response
	 * with details provided by the exception.
	 *
	 * @param apiEx The custom exception that occurred.
	 * @return A {@link ResponseEntity} containing the error response.
	 */
	@ExceptionHandler(ApiException.class)
	protected ResponseEntity<Object> handleApiException(ApiException apiEx) {
		return buildResponseEntity(apiEx.getError());
	}
}
