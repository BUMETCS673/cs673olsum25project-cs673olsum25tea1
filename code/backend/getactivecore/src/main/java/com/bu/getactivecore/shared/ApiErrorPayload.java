package com.bu.getactivecore.shared;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

/**
 * Represents a standardized error payload for API responses.
 * <p>
 * This class is used by
 * {@link com.bu.getactivecore.service.security.GlobalExceptionHandler} to build
 * consistent error responses sent back to API clients.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public final class ApiErrorPayload {

	/**
	 * Timestamp when the error occurred, formatted as "dd-MM-yyyy hh:mm:ss".
	 */
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
	@Builder.Default
	private final LocalDateTime timestamp = LocalDateTime.now();

	/**
	 * HTTP status code for the error response.
	 */
	private HttpStatus status;

	/**
	 * Application-specific error code enum.
	 */
	private ErrorCode errorCode;

	/**
	 * Detailed error message.
	 */
	private String message;

	/**
	 * List of validation errors, if any.
	 */
	private Map<String, List<String>> validationErrors;

	/**
	 * Detailed debug message, typically exception details.
	 */
	private String debugMessage;
}