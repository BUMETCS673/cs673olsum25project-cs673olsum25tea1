package com.bu.getactivecore.service.registration.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.Set;

/**
 * Validator for the {@link ValidPassword} annotation.
 */
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        Set<String> violations = new HashSet<>();

        if (password == null || password.isBlank()) {
            violations.add("Password cannot be blank");
        } else {
            if (password.length() < 8) {
                violations.add("Password must be at least 8 characters long");
            }
            if (password.length() > 32) {
                violations.add("Password must be at most 32 characters long");
            }
            if (!password.matches(".*[A-Z].*")) {
                violations.add("Password must contain at least one uppercase letter");
            }
            if (!password.matches(".*\\d.*")) {
                violations.add("Password must contain at least one digit");
            }
            if (!password.matches(".*[ !\"#$%&'()*+,-./:;<=>?@\\[\\]^_`{|}~].*")) {
                violations.add("Password must contain at least one special character");
            }
        }

        if (!violations.isEmpty()) {
            context.disableDefaultConstraintViolation();
            for (String message : violations) {
                context.buildConstraintViolationWithTemplate(message)
                        .addConstraintViolation();
            }
            return false;
        }
        return true;
    }
}
