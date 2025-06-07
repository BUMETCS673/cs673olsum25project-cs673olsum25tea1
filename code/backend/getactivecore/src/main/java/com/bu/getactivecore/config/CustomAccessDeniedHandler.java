package com.bu.getactivecore.config;


import com.bu.getactivecore.shared.ApiErrorPayload;
import com.bu.getactivecore.shared.ApiErrorResponse;
import com.bu.getactivecore.shared.ErrorCode;
import com.bu.getactivecore.shared.exception.ResourceAccessDeniedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * CustomAccessDeniedHandler is a Spring Security component that handles access-denied exceptions
 * thrown during the **authorization phase** within the Spring Security filter chain.
 *
 * <h2>When This Handler is Invoked:</h2>
 * <ul>
 *   <li>User is <strong>authenticated</strong>, but lacks required permissions or roles.</li>
 *   <li>Access is denied due to `@PreAuthorize`, method-level security, or URL-level authorization.</li>
 *   <li>Thrown exception is of type {@link org.springframework.security.access.AccessDeniedException}.</li>
 * </ul>
 *
 * <h2>When This Handler is <strong>Not</strong> Invoked:</h2>
 * <ul>
 *   <li>Exceptions thrown in the controller, service or application logic (i.e: business logic exceptions).</li>
 *   <li>Authentication failures (e.g: bad credentials) — those are handled by the {@code AuthenticationEntryPoint}.</li>
 *   <li>Manual throwing of custom exceptions like {@code ResourceAccessDeniedException} from service/controller.</li>
 * </ul>
 *
 * <h2>Handling Exceptions in Application Code</h2>
 * <p>
 * If you want to convert custom exceptions (like {@code ResourceAccessDeniedException}) thrown from controllers
 * or service methods into proper HTTP responses, use a {@code @RestControllerAdvice} with {@code @ExceptionHandler}.
 * </p>
 *
 * <h2>Error Format</h2>
 * This handler serializes an {@link ApiErrorPayload} into JSON and returns it as the response body with a
 * {@code 403 Forbidden} HTTP status.
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper; // Jackson mapper to serialize ApiErrorResponse

    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex) throws IOException {
        ApiErrorPayload apiErrorPayload;
        if (ex instanceof ResourceAccessDeniedException resourceEx) {
            apiErrorPayload = resourceEx.getError();
        } else {
            apiErrorPayload = ApiErrorPayload.builder()
                    .status(HttpStatus.FORBIDDEN)
                    .errorCode(ErrorCode.RESOURCE_ACCESS_DENIED)
                    .message(ex.getMessage() != null ? "Access Denied: " + ex.getMessage() : "Access Denied")
                    .debugMessage(ex.getLocalizedMessage())
                    .build();
        }
        ApiErrorResponse errorResponse = new ApiErrorResponse(apiErrorPayload);

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}

