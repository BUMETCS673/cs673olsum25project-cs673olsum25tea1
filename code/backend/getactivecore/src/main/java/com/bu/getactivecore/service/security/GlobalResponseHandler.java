package com.bu.getactivecore.service.security;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.bu.getactivecore.shared.ApiErrorResponse;
import com.bu.getactivecore.shared.ApiResponse;

/**
 * Intercepts all controller responses and wraps them into a standardized API
 * response format. It ensures that every successful response body is wrapped
 * inside an {@link ApiResponse} object, providing a consistent structure for
 * API consumers.
 */
@ControllerAdvice
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		// Only wrap non-error responses
		return true;
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
			Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
			ServerHttpResponse response) {
		// Don't wrap if already an error or already wrapped
		if (body instanceof ApiResponse || body instanceof ApiErrorResponse) {
			return body;
		}
		// If the body is null, return null to avoid wrapping
		return body == null ? null : new ApiResponse<>(body);
	}
}