package com.bu.getactivecore.service.users;

import static com.bu.getactivecore.shared.ErrorCode.WRONG_CREDENTIALS;
import static com.bu.getactivecore.shared.ErrorCode.AVATAR_SIZE_EXCEEDS_LIMIT;
import static com.bu.getactivecore.shared.ErrorCode.WRONG_CREDENTIALS;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bu.getactivecore.model.users.UserPrincipal;
import com.bu.getactivecore.model.users.Users;
import com.bu.getactivecore.repository.UserRepository;
import com.bu.getactivecore.service.jwt.api.JwtApi;
import com.bu.getactivecore.service.users.api.UserInfoApi;
import com.bu.getactivecore.service.users.entity.*;
import com.bu.getactivecore.shared.ApiErrorPayload;
import com.bu.getactivecore.shared.ErrorCode;
import com.bu.getactivecore.shared.exception.ApiException;
import com.bu.getactivecore.shared.validation.AccountStateChecker;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Core logic for managing user related operations.
 */
@Slf4j
@Service
public class UsersService implements UserInfoApi {

	private final AuthenticationManager m_authManager;

	private final JwtApi m_jwtApi;

	private final AccountStateChecker m_accountStateChecker;

    private final UserRepository m_userRepo;

    private static final int MAX_AVATAR_SIZE = 3 * 1024 * 1024; // 3MB in bytes

	/**
	 * Constructor for UsersService.
	 *
	 * @param authManager used for login operations
	 */
	public UsersService(AuthenticationManager authManager, JwtApi jwtApi, AccountStateChecker accountStateChecker, UserRepository userRepo) {
		m_authManager = authManager;
		m_jwtApi = jwtApi;
		m_accountStateChecker = accountStateChecker;
		m_userRepo = userRepo;
	}

	@Override
	public LoginResponseDto loginUser(LoginRequestDto requestDto) {
		// Given unauthenticated credentials, use the authentication manager to
		// authenticate the user
		Authentication authentication = m_authManager.authenticate(
				new UsernamePasswordAuthenticationToken(requestDto.getUsername(), requestDto.getPassword()));
        
        // If authentication is successful, the user is logged in
        if (!authentication.isAuthenticated()) {
            ApiErrorPayload error = ApiErrorPayload.builder().status(UNAUTHORIZED).errorCode(WRONG_CREDENTIALS)
                    .message("Invalid credentials provided").build();
            throw new ApiException(error);
        }

        // Only verified users can access resources
        m_accountStateChecker.assertVerified(authentication);

        String token = m_jwtApi.generateToken(requestDto.getUsername());
        UserDto userDto = ((UserPrincipal) authentication.getPrincipal()).getUserDto();
        return new LoginResponseDto(token, userDto.getUsername(), userDto.getEmail(), userDto.getAvatar(), userDto.getAvatarUpdatedAt());
    }

    @Override
    @Transactional
    public UpdateAvatarResponseDto updateAvatar(String username, UpdateAvatarRequestDto requestDto) throws ApiException {
        // Validate avatar data size
        String base64Data = requestDto.getAvatarData().split(",")[1];
        byte[] imageData = Base64.getDecoder().decode(base64Data);
        if (imageData.length > MAX_AVATAR_SIZE) {
            ApiErrorPayload error = ApiErrorPayload.builder().status(BAD_REQUEST).errorCode(AVATAR_SIZE_EXCEEDS_LIMIT)
                    .message("Avatar size exceeds 3MB limit")
                    .build();
            throw new ApiException(error);
        }

        // Update user avatar
        Users user = m_userRepo.findByUsername(username)
                .orElseThrow(() -> {
                    ApiErrorPayload error = ApiErrorPayload.builder().status(NOT_FOUND).errorCode(WRONG_CREDENTIALS)
                            .message("User not found")
                            .build();
                    return new ApiException(error);
                });
        
        user.setAvatar(requestDto.getAvatarData());
        user.setAvatarUpdatedAt(LocalDateTime.now());
        m_userRepo.save(user);

        return new UpdateAvatarResponseDto(
            user.getAvatar(),
            user.getAvatarUpdatedAt()
        );
    }

}
