package com.bu.getactivecore.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utility class for extracting claims from JWT tokens in tests.
 */
public class JwtTestUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Extracts the value of a specific claim from a JWT token.
     *
     * @param jwt       The JWT token string.
     * @param claimName The name of the claim to extract.
     * @return The value of the claim as a String.
     * @throws Exception if the token is invalid or the claim is missing.
     */
    public static String extractClaim(String jwt, String claimName) throws Exception {
        String[] parts = jwt.split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid JWT token format.");
        }

        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        JsonNode payload = objectMapper.readTree(payloadJson);

        JsonNode claim = payload.get(claimName);
        if (claim == null || claim.isNull()) {
            throw new IllegalArgumentException("Claim '" + claimName + "' not found in JWT payload.");
        }

        return claim.asText();
    }
}