package com.bu.getactivecore.service.users;

import static com.bu.getactivecore.shared.ErrorCode.WRONG_CREDENTIALS;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.bu.getactivecore.model.users.UserPrincipal;
import com.bu.getactivecore.service.jwt.api.JwtApi;
import com.bu.getactivecore.service.users.api.UserInfoApi;
import com.bu.getactivecore.service.users.entity.LoginRequestDto;
import com.bu.getactivecore.service.users.entity.LoginResponseDto;
import com.bu.getactivecore.service.users.entity.UserDto;
import com.bu.getactivecore.shared.ApiErrorPayload;
import com.bu.getactivecore.shared.exception.ApiException;
import com.bu.getactivecore.shared.validation.AccountStateChecker;

import lombok.extern.slf4j.Slf4j;

/**
 * Core logic for managing user related operations.
 */
@Slf4j
@Service
public class UsersService implements UserInfoApi {

    private final AuthenticationManager m_authManager;

    private final JwtApi m_jwtApi;

    private final AccountStateChecker m_accountStateChecker;

    /**
     * Constructor for UsersService.
     *
     * @param authManager used for login operations
     */
    public UsersService(AuthenticationManager authManager, JwtApi jwtApi, AccountStateChecker accountStateChecker) {
        m_authManager = authManager;
        m_jwtApi = jwtApi;
        m_accountStateChecker = accountStateChecker;
    }

    @Override
    public LoginResponseDto loginUser(LoginRequestDto requestDto) {
        // Given unauthenticated credentials, use the authentication manager to authenticate the user
        Authentication authentication = m_authManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestDto.getUsername(), requestDto.getPassword()));

        // If authentication is successful, the user is logged in
        if (!authentication.isAuthenticated()) {
            ApiErrorPayload error = ApiErrorPayload.builder().status(UNAUTHORIZED).errorCode(WRONG_CREDENTIALS)
                    .message("Invalid credentials provided")
                    .build();
            throw new ApiException(error);
        }

        // Only verified users can access resources
        m_accountStateChecker.assertVerified(authentication);

        String token = m_jwtApi.generateToken(requestDto.getUsername());
        UserDto userDto = ((UserPrincipal) authentication.getPrincipal()).getUserDto();
        return new LoginResponseDto(token, userDto.getUsername(), userDto.getEmail());
    }
}
