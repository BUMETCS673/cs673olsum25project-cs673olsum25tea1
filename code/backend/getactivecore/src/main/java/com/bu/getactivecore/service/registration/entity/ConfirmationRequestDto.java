package com.bu.getactivecore.service.registration.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;

/**
 * Registration confirmation request DTO.
 */
@Value
public class ConfirmationRequestDto {

	/**
	 * The token used to confirm user's registration.
	 */
	@NotBlank(message = "Token cannot be blank")
	String token;
}
