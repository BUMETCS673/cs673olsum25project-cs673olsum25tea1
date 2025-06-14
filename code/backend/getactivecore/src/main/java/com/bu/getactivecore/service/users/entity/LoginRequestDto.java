package com.bu.getactivecore.service.users.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;

/**
 * The DTO for user login requests.
 */
@Value
public class LoginRequestDto {

	@NotBlank(message = "Username must not be blank")
	String username;

	@NotBlank(message = "Password must not be blank")
	String password;
}
