package com.bu.getactivecore.service.registration.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Custom annotation for validating that an email address belongs to the
 * '@bu.edu' domain. This annotation can be used in conjunction with a custom
 * validator to ensure that the email provided by the user is a valid Boston
 * University email address.
 */
@Documented
@Constraint(validatedBy = { BuEmailValidator.class })
@Target({ java.lang.annotation.ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBuEmail {
	String message() default "Valid '@bu.edu' email is required";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
