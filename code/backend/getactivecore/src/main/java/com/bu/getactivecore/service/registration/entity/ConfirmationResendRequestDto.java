package com.bu.getactivecore.service.registration.entity;

import com.bu.getactivecore.service.registration.validation.ValidBuEmail;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

/**
 * The DTO for resending confirmation request.
 */
@Value
public class ConfirmationResendRequestDto {

	@ValidBuEmail
	String email;

	@NotBlank(message = "Username cannot be blank")
	@Size(min = 2, message = "Username must be at least 2 characters")
	@Size(max = 20, message = "Username can be most 20 characters")
	String username;
}
