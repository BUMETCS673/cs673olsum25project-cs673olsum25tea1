package com.bu.getactivecore.service.users;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bu.getactivecore.service.users.api.UserInfoApi;
import com.bu.getactivecore.service.users.entity.LoginRequestDto;
import com.bu.getactivecore.service.users.entity.LoginResponseDto;
import com.bu.getactivecore.shared.exception.ApiException;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * Entry point for all user related requests.
 */
@Slf4j
@RestController
@RequestMapping(value = "/v1")
public class UsersController {

    private final UserInfoApi m_userInfoApi;

    /**
     * Constructs the UsersController.
     *
     * @param userInfoApi used to fetch and manage user information
     */
    public UsersController(UserInfoApi userInfoApi) {
        m_userInfoApi = userInfoApi;
    }

    @PostMapping(path = "/login", consumes = "application/json")
    public LoginResponseDto loginUser(@Valid @RequestBody LoginRequestDto loginUserDto) throws ApiException {
        log.debug("Got request at /login");
        return m_userInfoApi.loginUser(loginUserDto);
    }
}