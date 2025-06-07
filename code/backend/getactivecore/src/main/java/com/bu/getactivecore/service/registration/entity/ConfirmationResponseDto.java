package com.bu.getactivecore.service.registration.entity;

import lombok.Builder;
import lombok.Data;

/**
 * Registration confirmation response DTO.
 */
@Builder
@Data
public class ConfirmationResponseDto {

    /**
     * The status of the registration confirmation.
     */
    private RegistrationStatus status;
}
