package com.bu.getactivecore.service.users.entity;

import com.bu.getactivecore.model.users.AccountState;
import com.bu.getactivecore.model.users.Users;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO for exposing user data.
 * <p>
 * For internal use only, not to be exposed in public APIs.
 */
@Builder
@Data
public class UserDto {

	/**
	 * UUID of the user.
	 */
	private String userId;

	/**
	 * The email of the user.
	 */
	private String email;

	/**
	 * The username of the user.
	 */
	private String username;

	/**
	 * The password of the user. Note: This field should not be exposed in public
	 * APIs for security reasons.
	 */
	private String password;

    /**
     * The state of this user's account.
     */
    private AccountState accountState;

    /**
     * The avatar of the user in Base64 format.
     */
    private String avatar;

    /**
     * The last update time of the avatar.
     */
    private LocalDateTime avatarUpdatedAt;

    /**
     * Converts a Users entity to a UserDto.
     *
     * @param user the Users entity
     * @return the UserDto
     */
    public static UserDto of(Users user) {
        return UserDto.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .username(user.getUsername())
                .password(user.getPassword())
                .accountState(user.getAccountState())
                .avatar(user.getAvatar())
                .avatarUpdatedAt(user.getAvatarUpdatedAt())
                .build();
    }

    /**
     * Builds an unverified {@link Users} entity from the provided information.
     *
     * @param email           the email of the user
     * @param username        the username of the user
     * @param encodedPassword the encoded password of the user
     * @return an unverified Users entity
     */
    public static Users from(String email, String username, String encodedPassword) {
        return Users.builder()
                .email(email)
                .username(username)
                .password(encodedPassword)
                .accountState(AccountState.UNVERIFIED)
                .build();
	}
}
