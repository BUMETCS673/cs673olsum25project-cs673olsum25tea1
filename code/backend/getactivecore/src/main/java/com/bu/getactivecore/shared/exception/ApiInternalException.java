package com.bu.getactivecore.shared.exception;

import com.bu.getactivecore.shared.ApiErrorPayload;
import com.bu.getactivecore.shared.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Generic internal server error exception.
 */
@Getter
public class ApiInternalException extends ApiException {

    private final ApiErrorPayload error;

    /**
     * Generic internal server error exception thrown when an unexpected error occurs.
     *
     * @param message detailed error message about the internal server error
     */
    public ApiInternalException(String message) {
        super(message);
        error = ApiErrorPayload.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .errorCode(ErrorCode.INTERNAL_SERVER_ERROR)
                .message(message)
                .build();
    }

}