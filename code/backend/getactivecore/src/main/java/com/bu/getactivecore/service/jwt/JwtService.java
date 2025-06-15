package com.bu.getactivecore.service.jwt;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.bu.getactivecore.service.jwt.api.JwtApi;
import com.bu.getactivecore.shared.exception.ApiException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for generating, parsing, and validating JSON Web Tokens
 * (JWTs).
 */
@Slf4j
@Service
public class JwtService implements JwtApi {

	private final JwtProperties jwtProp;

	/**
	 * Constructor for JwtService.
	 *
	 * @param jwtProperties used for token generation and validation.
	 */
	public JwtService(JwtProperties jwtProperties) {
		jwtProp = jwtProperties;
	}

	@PostConstruct
	public void init() throws NoSuchAlgorithmException {
		if (jwtProp.getSecretKey() == null || jwtProp.getSecretKey().isEmpty()) {
			log.warn("No secret key provided in JwtProperties, generating a new one.");

			KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
			SecretKey key = keyGen.generateKey();
			jwtProp.setSecretKey(Base64.getEncoder().encodeToString(key.getEncoded()));
		}
	}

	/**
	 * Retrieves the secret key used for signing and verifying JWT tokens.
	 *
	 * @return the secret key as a {@link SecretKey}
	 */
	private SecretKey getKey() {
		byte[] key = Decoders.BASE64.decode(jwtProp.getSecretKey());
		return Keys.hmacShaKeyFor(key);
	}

	/**
	 * Extracts a specific claim from the JWT token.
	 *
	 * @param token         the JWT token
	 * @param claimResolver the function to extract the claim
	 * @param <T>           the type of the claim
	 * @return the extracted claim
	 * @throws JwtException if token parsing fails or the claim is not found
	 */
	private <T> T extractClaim(String token, Function<Claims, T> claimResolver) throws JwtException {
		final Claims claims = extractAllClaims(token);
		return claimResolver.apply(claims);
	}

	/**
	 * Extracts all claims (payload data) from the provided JWT.
	 *
	 * @param token the JWT string
	 * @return a {@link Claims} object containing all claims
	 * @throws ApiException if token parsing fails
	 */
	private Claims extractAllClaims(String token) throws JwtException {
		return Jwts.parser() //
				.verifyWith(getKey()) //
				.build() //
				.parseSignedClaims(token) //
				.getPayload();
	}

	/**
	 * Checks if the provided JWT token is expired.
	 *
	 * @param token the JWT token to check
	 * @return true if the token is expired, false otherwise
	 */
	private boolean isTokenExpired(String token) {
		return getExpiration(token).before(new Date());
	}

	/**
	 * Extracts the expiration date from the JWT token.
	 *
	 * @param token the JWT token
	 * @return the expiration date as a {@link Date}
	 */
	private Date getExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	@Override
	public String generateToken(String username, TokenClaimType claimType) {
		if (username == null || username.isBlank()) {
			throw new ApiException("Username must not be blank");
		}

		Map<String, Object> claims = new HashMap<>();
		if (claimType != null) {
			claims.put(JwtApi.TOKEN_CLAIM_TYPE_KEY, claimType.name());
		}
		return Jwts.builder().claims().add(claims).subject(username).issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis() + jwtProp.getExpirationMs())) // 30 minutes
				.and().signWith(getKey()).compact();
	}

	@Override
	public boolean isValid(String token) {
		try {
			extractAllClaims(token);
			return true;
		} catch (JwtException e) {
			log.error("Invalid JWT token: {}", e.getMessage(), e);
		}
		return false;
	}

	@Override
	public boolean validateToken(String token) throws JwtException {
		extractAllClaims(token);
		return true;
	}

	@Override
	public boolean validateToken(String token, UserDetails userDetails) throws JwtException {
		final String username = extractClaim(token, Claims::getSubject);
		if (!username.equals(userDetails.getUsername())) {
			log.error("Invalid JWT token: expected {}, got {}", userDetails.getUsername(), username);
			return false;
		}
		return true;
	}

	@Override
	public String getUsername(String token) {
		// Since username is part of the claims, we can extract it directly
		return extractClaim(token, Claims::getSubject);
	}

	@Override
	public String getClaim(String token, String claimName) throws JwtException {
		Claims claims = extractAllClaims(token);
		return claims.get(claimName, String.class);
	}

}
