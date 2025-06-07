package com.bu.getactivecore.service.users.api;

import com.bu.getactivecore.service.users.entity.LoginRequestDto;
import com.bu.getactivecore.service.users.entity.LoginResponseDto;

/**
 * Interface for managing user operations.
 */
public interface UserInfoApi {
    /**
     * Authenticates a user with the given login credentials.
     *
     * @param userDto Data Transfer Object containing login credentials.
     * @return {@link LoginResponseDto} containing authentication token on success.
     */
    LoginResponseDto loginUser(LoginRequestDto userDto);

}
