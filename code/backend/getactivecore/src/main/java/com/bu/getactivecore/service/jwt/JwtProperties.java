package com.bu.getactivecore.service.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties class for JWT token settings.
 *
 * <p>
 * This class is automatically populated with values from the application's
 * configuration file (e.g., {@code application.properties} using the prefix
 * {@code jwt.token}.
 *
 * <p>
 * Example in {@code application.properties}:
 * 
 * <pre>
 * jwt.token.secret=secure-base64-secret
 * jwt.token.expiration-ms=1800000
 * </pre>
 *
 * <p>
 * The values can also be overridden using environment variables:
 * <ul>
 * <li>{@code JWT_TOKEN_SECRET}</li>
 * <li>{@code JWT_TOKEN_EXPIRATION_MS}</li>
 * </ul>
 *
 * <p>
 * This allows for secure management of secrets and flexible test configuration.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "jwt.token")
public class JwtProperties {

	/**
	 * Holds the Base64-encoded secret key used for signing and verifying JWT
	 * tokens.
	 *
	 * <p>
	 * This key is essential for ensuring the integrity and authenticity of JWTs by
	 * enabling HMAC-SHA256 signing. The secret must remain constant across app
	 * restarts to validate issued tokens.
	 */
	private String secretKey;

	/**
	 * Token expiration time in milliseconds.
	 * <p>
	 * Defines how long the token is valid before it expires.
	 * </p>
	 */
	private long expirationMs;
}