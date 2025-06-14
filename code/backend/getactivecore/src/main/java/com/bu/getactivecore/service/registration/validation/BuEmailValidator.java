package com.bu.getactivecore.service.registration.validation;

import java.util.HashSet;
import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for the {@link ValidBuEmail} annotation.
 */
@SuppressWarnings("java:S2692")
public class BuEmailValidator implements ConstraintValidator<ValidBuEmail, String> {

	@Override
	public boolean isValid(String email, ConstraintValidatorContext context) {
		Set<String> violations = new HashSet<>();
		if (email == null || email.isBlank()) {
			violations.add("Email cannot be blank");
		} else {
			if (!email.endsWith("@bu.edu")) {
				violations.add("Email must contain '@bu.edu' domain");
			}
			if (email.indexOf('@') <= 0 || email.chars().filter(c -> c == '@').count() != 1) {
				violations.add("Invalid email format provided");
			}
		}
		if (!violations.isEmpty()) {
			context.disableDefaultConstraintViolation();
			for (String message : violations) {
				context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			}
			return false;
		}
		return true;
	}
}
