package com.bu.getactivecore.service.users.entity;

import lombok.Value;
import java.time.LocalDateTime;

/**
 * The login response DTO containing the authentication token. This token should
 * be used for subsequent requests to authenticate the user.
 */
@Value
public class LoginResponseDto {

    /**
     * The JWT token for the authenticated user, which should be included in the Authorization header for subsequent requests to
     * access protected resources.
     */
    String token;
    String username;
    String email;
    String avatar;
    LocalDateTime avatarUpdatedAt;

}
