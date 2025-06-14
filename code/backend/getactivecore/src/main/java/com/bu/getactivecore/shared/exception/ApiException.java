package com.bu.getactivecore.shared.exception;

import com.bu.getactivecore.shared.ApiErrorPayload;
import com.bu.getactivecore.shared.ErrorCode;

import lombok.Getter;

/**
 * ApiException is a custom exception class used to represent
 * application-specific errors in a structured and consistent manner.
 *
 * <p>
 * This exception is typically thrown when an error occurs that needs to be
 * communicated to the client with additional details such as an error code,
 * HTTP status, and a user-friendly error message.
 * </p>
 */
@Getter
public class ApiException extends RuntimeException {

	private final ApiErrorPayload error;

	/**
	 * Constructs a new ApiException with a default error code and message.
	 *
	 * @param message detailed error message
	 */
	public ApiException(String message) {
		super(message);
		this.error = ApiErrorPayload.builder().errorCode(ErrorCode.GENERAL_ERROR).message(message).build();
	}

	/**
	 * Constructs a new ApiException with the specified error payload.
	 *
	 * @param error the ApiErrorPayload containing structured details about the
	 *              exception
	 */
	public ApiException(ApiErrorPayload error) {
		super(error.getMessage());
		this.error = error;
	}
}