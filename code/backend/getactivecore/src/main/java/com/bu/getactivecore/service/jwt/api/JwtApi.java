package com.bu.getactivecore.service.jwt.api;

import com.bu.getactivecore.shared.exception.ApiException;
import io.jsonwebtoken.JwtException;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Interface for JWT (JSON Web Token) operations.
 */
public interface JwtApi {

    /**
     * The key used to store the type of claim in the JWT token.
     */
    String TOKEN_CLAIM_TYPE_KEY = "type";

    /**
     * Generates a JWT token for the given username with an optional claim type.
     *
     * @param username  the username for which to generate the token
     * @param claimType the type of claim to embed in the token, can be null
     * @return the generated JWT token
     * @throws ApiException when username is null or empty
     */
    String generateToken(String username, TokenClaimType claimType) throws ApiException;

    /**
     * Generates a JWT token for the given username.
     *
     * @param username the username for which to generate the token
     * @return the generated JWT token
     * @throws ApiException when username is null or empty
     */
    default String generateToken(String username) throws ApiException {
        return generateToken(username, null);
    }

    /**
     * Retrieves the username (subject) embedded in the token.
     *
     * @param token to get username from
     * @return the username extracted from the token or null if the token is invalid
     */
    String getUsername(String token);

    /**
     * Retrieves the claims embedded in the token.
     *
     * @param token     to get claims from
     * @param claimName the name of the claim to retrieve
     * @return the claims extracted from the token or null if the token is invalid
     */
    String getClaim(String token, String claimName) throws JwtException;

    /**
     * Validates the given JWT token.
     *
     * @param token the JWT token to validate
     * @return true if the token is valid, false otherwise
     */
    boolean isValid(String token);

    /**
     * Validates the given JWT token.
     *
     * @param token the JWT token to validate
     * @return true if the token is valid, false otherwise
     * @throws JwtException if the token is invalid
     */
    boolean validateToken(String token) throws JwtException;

    /**
     * Validates the given JWT token against the provided user details.
     *
     * @param token       the JWT token to validate
     * @param userDetails the user details to validate against
     * @return true if the token is valid, false otherwise
     * @throws JwtException if the token is invalid
     */
    boolean validateToken(String token, UserDetails userDetails) throws JwtException;

    /**
     * Enum representing different types of claims that can be embedded in a JWT token.
     */
    enum TokenClaimType {
        /**
         * Claim type for registration confirmation tokens.
         */
        REGISTRATION_CONFIRMATION,
    }
}
