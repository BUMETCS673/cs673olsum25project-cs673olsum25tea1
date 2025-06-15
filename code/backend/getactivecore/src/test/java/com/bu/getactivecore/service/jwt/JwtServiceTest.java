package com.bu.getactivecore.service.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;

import com.bu.getactivecore.shared.exception.ApiException;

class JwtServiceTest {

	private JwtProperties jwtProperties;

	private JwtService jwtService;

	@BeforeEach
	void setup() {
		jwtProperties = new JwtProperties();
		jwtProperties.setSecretKey(Base64.getEncoder().encodeToString("This-is-a-very-secure-secret-key!".getBytes()));
		jwtService = new JwtService(jwtProperties);
	}

	@Test
	void given_expired_token_then_token_is_not_valid() throws NoSuchAlgorithmException {
		jwtProperties.setExpirationMs(2 * 1000L);
		jwtService.init();

		String token = jwtService.generateToken("testuser");
		assertNotNull(token);

		CountDownLatch latch = new CountDownLatch(1);
		try {
			latch.await(jwtProperties.getExpirationMs() + 1000, java.util.concurrent.TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		assertFalse(jwtService.isValid(token));
	}

	@Test
	void given_valid_token_then_modified_fields_then_token_is_invalid() throws NoSuchAlgorithmException {
		jwtProperties.setExpirationMs(10 * 1000L); // 10 seconds
		jwtService.init();

		String validToken = jwtService.generateToken("testuser");
		assertNotNull(validToken);
		assertTrue(jwtService.isValid(validToken));

		// Tamper the payload (middle part of the JWT)
		String[] parts = validToken.split("\\.");
		assertEquals(3, parts.length);

		String tamperedPayload = Base64.getUrlEncoder().withoutPadding()
				.encodeToString("{\"sub\":\"some_other_user\"}".getBytes());

		String tamperedToken = parts[0] + "." + tamperedPayload + "." + parts[2];

		// Now the token should be considered invalid due to signature mismatch
		assertFalse(jwtService.isValid(tamperedToken));
	}

	@Test
	void verify_valid_token_are_generated() throws NoSuchAlgorithmException {
		jwtProperties.setExpirationMs(10 * 1000L);
		jwtService.init();

		String token = jwtService.generateToken("testuser");
		assertNotNull(token);

		assertTrue(jwtService.isValid(token));
	}

	@Test
	void given_blank_username_then_exception_is_thrown() throws NoSuchAlgorithmException {
		jwtProperties.setExpirationMs(10 * 1000L);
		jwtService.init();
		assertThrows(ApiException.class, () -> jwtService.generateToken(""));
	}

	@Test
	void given_valid_token_then_another_user_cannot_access() throws NoSuchAlgorithmException {
		jwtProperties.setExpirationMs(10 * 1000L);
		jwtService.init();

		// Given a valid token of a user
		String token = jwtService.generateToken("testuser");
		assertNotNull(token);
		assertTrue(jwtService.isValid(token));

		// Simulate a different user trying to access resources with the another user's
		// token
		UserDetails userDetails = Mockito.mock(UserDetails.class);
		Mockito.when(userDetails.getUsername()).thenReturn("another_valid_user_logged_in");
		assertFalse(jwtService.validateToken(token, userDetails));
	}

	@Test
	void given_valid_token_then_no_exception_is_thrown() throws NoSuchAlgorithmException {
		jwtProperties.setExpirationMs(10 * 1000L);
		jwtService.init();

		String token = jwtService.generateToken("testuser");
		assertNotNull(token);

		UserDetails userDetails = Mockito.mock(UserDetails.class);
		Mockito.when(userDetails.getUsername()).thenReturn("testuser");
		assertTrue(jwtService.validateToken(token, userDetails));
	}
}