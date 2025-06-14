package com.bu.getactivecore.service.security;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.bu.getactivecore.shared.ApiErrorPayload;
import com.bu.getactivecore.shared.ApiErrorResponse;
import com.bu.getactivecore.shared.exception.ApiException;
import com.bu.getactivecore.shared.exception.ResourceAccessDeniedException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * {@code ApiExceptionHandler} is a global exception handler that applies to all
 * REST controllers. It uses Spring's
 * {@link org.springframework.web.bind.annotation.RestControllerAdvice} to
 * intercept and handle exceptions thrown by application-level code (e.g.,
 * controllers, services).
 *
 * <p>
 * This class is responsible for:
 * <ul>
 * <li>Catching and handling uncaught exceptions thrown from controller methods
 * or service layers.</li>
 * <li>Mapping application-specific exceptions (e.g., {@code ApiException},
 * {@code ResourceAccessDeniedException}) to structured error responses with
 * HTTP status codes.</li>
 * <li>Returning errors in a consistent format such as {@link ApiErrorResponse},
 * containing a {@link ApiErrorPayload}.</li>
 * </ul>
 *
 * <h2>When This Handler is Invoked:</h2>
 * <ul>
 * <li>Exceptions thrown from inside controller methods or service methods that
 * bubble up to the controller layer.</li>
 * <li>Custom exceptions like {@code ApiException},
 * {@code ResourceNotFoundException}, etc.</li>
 * <li>Spring framework exceptions such as
 * {@code MethodArgumentNotValidException},
 * {@code HttpMessageNotReadableException}.</li>
 * </ul>
 *
 * <h2>When This Handler is <strong>Not</strong> Invoked:</h2>
 * <ul>
 * <li>Exceptions thrown during authentication or authorization phase inside
 * Spring Security filters.</li>
 * <li>Those are handled by:
 * <ul>
 * <li>{@link org.springframework.security.web.access.AccessDeniedHandler}
 * (e.g., {@link com.bu.getactivecore.config.CustomAccessDeniedHandler})</li>
 * <li>{@link org.springframework.security.web.AuthenticationEntryPoint} for
 * authentication failures</li>
 * </ul>
 * </li>
 * </ul>
 */
@RestControllerAdvice
public class ApiExceptionHandler {

	private final ObjectMapper objectMapper;

	public ApiExceptionHandler(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@ExceptionHandler(ResourceAccessDeniedException.class)
	public ResponseEntity<ApiErrorResponse> handleAccessDenied(ResourceAccessDeniedException ex) {
		ApiErrorPayload errorPayload = ex.getError();
		return ResponseEntity.status(errorPayload.getStatus()).body(new ApiErrorResponse(errorPayload));
	}

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ApiErrorResponse> handleApiException(ApiException ex) {
		return ResponseEntity.status(ex.getError().getStatus()).body(new ApiErrorResponse(ex.getError()));
	}
}
