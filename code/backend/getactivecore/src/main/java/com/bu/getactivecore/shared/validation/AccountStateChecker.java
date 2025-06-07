package com.bu.getactivecore.shared.validation;

import com.bu.getactivecore.model.users.UserPrincipal;
import com.bu.getactivecore.service.users.entity.UserDto;
import com.bu.getactivecore.shared.ApiErrorPayload;
import com.bu.getactivecore.shared.ErrorCode;
import com.bu.getactivecore.shared.exception.AccountVerificationException;
import com.bu.getactivecore.shared.exception.ApiInternalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


/**
 * This component checks if the user account is the expected state to access resources.
 */
@Slf4j
@Component("accountChecker")
public class AccountStateChecker {

    /**
     * Checks if the user account is verified.
     *
     * @param authentication the current user's authentication object
     * @return true if the user account is verified, otherwise throws an exception
     * @throws AccountVerificationException if the user account is not verified
     * @throws ApiInternalException         if the account is in an unexpected state
     */
    public boolean assertVerified(Authentication authentication) throws AccountVerificationException, ApiInternalException {
        UserDto userDto = ((UserPrincipal) authentication.getPrincipal()).getUserDto();
        switch (userDto.getAccountState()) {
            case UNVERIFIED -> {
                ApiErrorPayload error = ApiErrorPayload.builder()
                        .errorCode(ErrorCode.VERIFIED_ACCOUNT_REQUIRED)
                        .message("User account is not verified")
                        .validationErrors(Map.of("accountState", List.of("User has not verified their account")))
                        .build();
                throw new AccountVerificationException(error);
            }
            case VERIFIED -> {
                return true;
            }
            default -> {
                log.error("Unexpected account state {} of user {}", userDto.getAccountState(), userDto);
                throw new ApiInternalException("Account is in unexpected state " + userDto.getAccountState());
            }
        }
    }
}
