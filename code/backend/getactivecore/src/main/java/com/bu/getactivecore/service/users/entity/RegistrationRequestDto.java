package com.bu.getactivecore.service.users.entity;

import lombok.Value;

@Value
public class RegistrationRequestDto {
    String email;
    String username;
    String password;
}
