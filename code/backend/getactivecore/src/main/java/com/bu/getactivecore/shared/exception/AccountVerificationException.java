package com.bu.getactivecore.shared.exception;

import com.bu.getactivecore.shared.ApiErrorPayload;

import lombok.Getter;

/**
 * {@code AccountVerificationException} is a custom exception that represents an
 * access denial due to the user's account not being in a required verification
 * state.
 *
 * <p>
 * This exception is a specialization of {@link ResourceAccessDeniedException}
 * and is intended to be thrown when a user attempts to access a secured
 * resource but their account is not verified, thereby violating the access
 * control rules of the application.
 * </p>
 */
@Getter
public class AccountVerificationException extends ResourceAccessDeniedException {

	/**
	 * Constructs a new {@code AccountVerificationException} with the provided error
	 * payload.
	 *
	 * @param error the {@link ApiErrorPayload} containing structured details about
	 *              the exception
	 */
	public AccountVerificationException(ApiErrorPayload error) {
		super(error);
	}
}
