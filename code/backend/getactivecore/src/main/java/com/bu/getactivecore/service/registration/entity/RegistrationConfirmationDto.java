package com.bu.getactivecore.service.registration.entity;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RegistrationConfirmationDto {

    private RegistrationStatus status;
}
