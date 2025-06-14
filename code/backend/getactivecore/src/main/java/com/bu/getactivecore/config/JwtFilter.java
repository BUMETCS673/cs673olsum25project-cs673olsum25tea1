package com.bu.getactivecore.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.bu.getactivecore.service.jwt.api.JwtApi;
import com.bu.getactivecore.service.security.CustomUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * A custom JWT filter that intercepts incoming HTTP requests to perform JWT
 * token validation.
 * <p>
 * This filter extracts the token from the `Authorization` header, validates it,
 * and sets the authentication context if the token is valid. It ensures that
 * authentication is only set if the user is not already authenticated.
 */
@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {

	private final JwtApi m_jwtApi;

	private final ApplicationContext m_appContext;

	private final RequestMatcher m_publicPaths;

	/**
	 * Constructs the JwtFilter.
	 *
	 * @param jwtApi     used to validate JWT tokens and extract user information
	 * @param appContext used to access application beans
	 * @param skipPaths  used to define public paths that do not require
	 *                   authentication
	 */
	public JwtFilter(JwtApi jwtApi, ApplicationContext appContext, @Qualifier("skipPaths") RequestMatcher skipPaths) {
		m_jwtApi = jwtApi;
		m_appContext = appContext;
		m_publicPaths = skipPaths;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		return m_publicPaths.matches(request);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String header = request.getHeader("Authorization");
		String token = null;
		String username = null;

		if (header != null && header.startsWith("Bearer ")) {
			token = header.substring(7);
			username = m_jwtApi.getUsername(token);
		} else {
			log.warn("Authorization header is missing for request: {}", request.getRequestURI());
		}

		/*
		 * This ensures that only valid JWT tokens are granted access. And
		 * authentication is only set if not already present, preventing overwriting
		 * existing security contexts in a filter chain.
		 */
		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			UserDetails userDetails = m_appContext.getBean(CustomUserDetailsService.class).loadUserByUsername(username);
			if (m_jwtApi.validateToken(token, userDetails)) {
				// Set the authentication object into the so that downstream filters and
				// controllers can treat the request as authenticated.
				UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
						null, userDetails.getAuthorities());
				authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authToken);
			}
		}
		// Continue the filter chain
		filterChain.doFilter(request, response);
	}
}
