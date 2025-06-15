package com.bu.getactivecore.config;

import static com.bu.getactivecore.shared.Constants.PASSWORD_ENCODER_STRENGTH;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.bu.getactivecore.service.security.CustomUserDetailsService;

/**
 * This class configures the security settings for the application, including
 * stateless session management, JWT-based authentication, and custom access
 * controls. CSRF protection is disabled since the application is stateless and
 * uses JWT tokens for authentication.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	private final CustomUserDetailsService m_userDetailsService;

	private final JwtFilter m_jwtFilter;

	private final CustomAccessDeniedHandler m_customAccessDeniedHandler;

	private final RequestMatcher m_skipPaths;

	/**
	 * Constructs the SecurityConfig.
	 *
	 * @param userDetailsService        used to load user details for authentication
	 * @param jwtFilter                 used to filter requests and validate JWT
	 *                                  tokens
	 * @param customAccessDeniedHandler used to wrap access denied exceptions in a
	 *                                  custom response
	 * @param skipPaths                 used to define public paths that do not
	 *                                  require authentication
	 */
	public SecurityConfig(CustomUserDetailsService userDetailsService, JwtFilter jwtFilter,
			CustomAccessDeniedHandler customAccessDeniedHandler, @Qualifier("skipPaths") RequestMatcher skipPaths) {
		m_userDetailsService = userDetailsService;
		m_jwtFilter = jwtFilter;
		m_customAccessDeniedHandler = customAccessDeniedHandler;
		m_skipPaths = skipPaths;
	}

	/**
	 * Configures the HTTP security settings for the application.
	 *
	 * @param http the {@link HttpSecurity} object to configure
	 * @return the {@link SecurityFilterChain} instance for the application
	 * @throws Exception if an error occurs during configuration
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.cors(cors -> cors.configurationSource(corsConfigurationSource())).csrf(AbstractHttpConfigurer::disable)
                // Allow H2 Console to load in frame
				.headers(headers -> headers.addHeaderWriter(
						new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)))
				.exceptionHandling(accessDenied -> accessDenied.accessDeniedHandler(m_customAccessDeniedHandler))
				.authorizeHttpRequests(request -> request

						// Permit following endpoints without authentication
						.requestMatchers(m_skipPaths).permitAll()
						// All other requests require authentication
						.anyRequest().authenticated())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.addFilterBefore(m_jwtFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	/**
	 * Configures the authentication provider with a custom user details service and
	 * password encoder. This bean is used to authenticate users based on
	 * credentials stored in the system.
	 *
	 * @return the configured {@link AuthenticationProvider} instance
	 */
	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setPasswordEncoder(new BCryptPasswordEncoder(PASSWORD_ENCODER_STRENGTH));
		provider.setUserDetailsService(m_userDetailsService);
		return provider;
	}

	/**
	 * This bean is responsible for managing authentication processes in the
	 * application.
	 *
	 * @param config the {@link AuthenticationConfiguration} object
	 * @return the {@link AuthenticationManager} instance
	 * @throws Exception if an error occurs during the configuration
	 */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	/**
	 * Configures CORS settings for the application.
	 *
	 * @return a {@link CorsConfigurationSource} instance with the defined CORS
	 *         settings
	 */
	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of("*"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(false);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
