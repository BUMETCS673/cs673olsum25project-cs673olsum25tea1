package com.bu.getactivecore.service.registration;

import com.bu.getactivecore.service.registration.api.RegistrationApi;
import com.bu.getactivecore.service.registration.entity.ConfirmationRequestDto;
import com.bu.getactivecore.service.registration.entity.ConfirmationResendRequestDto;
import com.bu.getactivecore.service.registration.entity.ConfirmationResponseDto;
import com.bu.getactivecore.service.registration.entity.RegistrationRequestDto;
import com.bu.getactivecore.service.registration.entity.RegistrationResponseDto;
import com.bu.getactivecore.shared.exception.ApiException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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

    @PostMapping(path = "/register/confirmation", consumes = "application/json")
    public ConfirmationResponseDto verifyRegistration(@Valid @RequestBody ConfirmationRequestDto requestDto) throws ApiException {
        log.debug("Got request at /register/confirmation");
        return m_registrationApi.confirmRegistration(requestDto);
    }


    @PostMapping(path = "/register/confirmation/resend", consumes = "application/json")
    public ResponseEntity<Void> resendRegistrationToken(@Valid @RequestBody ConfirmationResendRequestDto requestDto) {
        log.debug("Got request at /register/resend-confirmation");
        m_registrationApi.resendConfirmation(requestDto);
        return ResponseEntity.noContent().build();
    }
}