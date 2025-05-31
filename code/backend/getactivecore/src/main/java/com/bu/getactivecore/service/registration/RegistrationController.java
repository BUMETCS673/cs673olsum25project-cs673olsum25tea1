package com.bu.getactivecore.service.registration;

import com.bu.getactivecore.service.registration.api.RegistrationApi;
import com.bu.getactivecore.service.registration.entity.ConfirmRegistrationRequestDto;
import com.bu.getactivecore.service.registration.entity.RegistrationConfirmationDto;
import com.bu.getactivecore.service.registration.entity.RegistrationRequestDto;
import com.bu.getactivecore.service.registration.entity.RegistrationResponseDto;
import com.bu.getactivecore.shared.exception.ApiException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Entry point for registration related operations.
 */
@Slf4j
@RestController
@RequestMapping(value = "/v1")
public class RegistrationController {

    private final RegistrationApi m_registrationApi;

    /**
     * Constructs the RegistrationController.
     *
     * @param registrationApi used to handle user registration and verification
     */
    public RegistrationController(RegistrationApi registrationApi) {
        m_registrationApi = registrationApi;
    }

    @PostMapping(path = "/register", consumes = "application/json")
    public RegistrationResponseDto registerUser(@Valid @RequestBody RegistrationRequestDto requestDto) throws ApiException {
        log.debug("Got request at /register");
        return m_registrationApi.registerUser(requestDto);
    }

    @PostMapping(path = "/register/confirm", consumes = "application/json")
    public RegistrationConfirmationDto verifyRegistration(@Valid @RequestBody ConfirmRegistrationRequestDto requestDto) throws ApiException {
        log.debug("Got request at /register/confirm");
        return m_registrationApi.confirmRegistration(requestDto);
    }
}