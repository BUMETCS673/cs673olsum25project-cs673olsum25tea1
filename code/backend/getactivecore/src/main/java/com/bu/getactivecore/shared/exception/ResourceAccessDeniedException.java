package com.bu.getactivecore.shared.exception;

import com.bu.getactivecore.shared.ApiErrorPayload;
import lombok.Getter;
import org.springframework.security.access.AccessDeniedException;

import static org.springframework.http.HttpStatus.FORBIDDEN;

/**
 * ResourceAccessDeniedException is a custom exception that extends AccessDeniedException.
 * It is used to indicate that access to a specific resource is denied, with additional
 * validation errors provided for more context.
 *
 * <p>This exception can be thrown when a user attempts to access a resource they do not
 * have permission to access.
 */
@Getter
public class ResourceAccessDeniedException extends AccessDeniedException {

    private final ApiErrorPayload error;

    /**
     * Specific exception thrown when access to a resource is denied.
     *
     * @param error the ApiErrorPayload containing error details
     */
    public ResourceAccessDeniedException(ApiErrorPayload error) {
        super(error.getMessage());
        this.error = error.toBuilder().status(FORBIDDEN).build();
    }
}
