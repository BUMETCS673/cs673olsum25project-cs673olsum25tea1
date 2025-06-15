package com.bu.getactivecore.shared;

/**
 * Response object for API errors.
 *
 * @param errors the error details
 */
public record ApiErrorResponse(ApiErrorPayload errors) {
}