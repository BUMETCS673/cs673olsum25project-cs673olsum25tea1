package com.bu.getactivecore.service.users.api;

import com.bu.getactivecore.service.users.entity.LoginRequestDto;
import com.bu.getactivecore.service.users.entity.LoginResponseDto;
import com.bu.getactivecore.service.users.entity.UpdateAvatarRequestDto;
import com.bu.getactivecore.service.users.entity.UpdateAvatarResponseDto;
import com.bu.getactivecore.shared.exception.ApiException;

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

    /**
     * Updates the avatar of the authenticated user.
     *
     * @param username The username of the authenticated user.
     * @param requestDto Data Transfer Object containing the new avatar data.
     * @return {@link UpdateAvatarResponseDto} containing the updated avatar information.
     * @throws ApiException if the update fails.
     */
    UpdateAvatarResponseDto updateAvatar(String username, UpdateAvatarRequestDto requestDto) throws ApiException;
}
