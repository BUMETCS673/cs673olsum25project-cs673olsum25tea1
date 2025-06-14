package com.bu.getactivecore.service.registration.entity;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RegistrationResponseDto {

	/**
	 * Confirmation token for informational purposes.
	 */
	private String token;

}
