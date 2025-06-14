package com.bu.getactivecore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Configuration class that defines public paths which do not require JWT
 * authentication.
 */
@Configuration
public class PublicPaths {

	/**
	 * Defines a RequestMatcher that matches public paths. These paths are
	 * accessible without authentication.
	 *
	 * @return a RequestMatcher for public paths
	 */
	@Bean("skipPaths")
	public RequestMatcher skipPaths() {
		return new OrRequestMatcher(new AntPathRequestMatcher("/h2-console/**"),
				new AntPathRequestMatcher("/v1/register"), new AntPathRequestMatcher("/v1/register/confirmation"),
				new AntPathRequestMatcher("/v1/register/confirmation/resend"), new AntPathRequestMatcher("/v1/health"),
				new AntPathRequestMatcher("/v1/login"));
	}
}